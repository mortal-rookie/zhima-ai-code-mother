package com.example.zhimaaicodemother.core.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.zhimaaicodemother.ai.model.message.*;
import com.example.zhimaaicodemother.ai.tools.BaseTool;
import com.example.zhimaaicodemother.ai.tools.ToolManager;
import com.example.zhimaaicodemother.model.entity.User;
import com.example.zhimaaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.example.zhimaaicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Set;

/**
 * Json 消息流处理器
 * 处理vue类型的复杂流式响应，包含工具调用
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    /**
     * 写入 chat_history.message 的最大长度。
     * 该字段是 TEXT（65535 字节，utf8mb4 下中文约 3 字节/字），
     * 而这条消息会包含本次生成写入的所有文件全文，必须截断后再入库。
     */
    private static final int MAX_HISTORY_MESSAGE_LENGTH = 20000;

    @Resource
    private ToolManager toolManager;

    /**
     * 处理TokenStream（VUE_PROJECT）
     * 解析Json消息并重组为完整的响应格式
     *
     * @param originFlux 原始流
     * @param chatHistoryService  聊天历史服务
     * @param appId 应用id
     * @param loginUser 登录用户
     * @return 处理后的流
     */
    public Flux<String > handle(Flux<String> originFlux,
                                ChatHistoryService chatHistoryService,
                                long appId, User loginUser){
        //收集数据用于生成后端记忆格式
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        //用于跟踪已经见过的工具Id，判断是否是第一次使用
        Set<String> seenToolIds = new HashSet<>();
        return originFlux
                .map(chunk ->{
                    return handleJsonMessageChunk(chunk,chatHistoryStringBuilder,seenToolIds);
                })
                .filter(StrUtil::isNotEmpty)//过滤空字符串
                .doOnComplete(()->{
                    //流式响应完成后，添加ai消息到对话历史
                    String aiResponse = chatHistoryStringBuilder.toString();
                    // 空内容不能入库：addChatMessage 对空白内容会直接抛参数异常，
                    if (StrUtil.isBlank(aiResponse)) {
                        log.warn("appId={} 的流式响应没有产生任何内容，跳过写入对话历史", appId);
                        return;
                    }
                    // 1）必须截断：chat_history.message 是 TEXT（65535 字节），
                    //    而这条 AI 消息里带着所有文件的全文（实测一次生成约 97KB），
                    //    直接入库必然抛 Data truncation: Data too long for column 'message'。
                    // 2）必须 try/catch：写历史只是副作用，绝不能让它把一次已经成功的生成
                    //    变成 error。实测正是这里抛异常，导致文件全部写完、构建都已经开始了，
                    //    前端却仍然显示"生成失败"。
                    try {
                        String toSave = StrUtil.maxLength(aiResponse, MAX_HISTORY_MESSAGE_LENGTH);
                        chatHistoryService.addChatMessage(appId, toSave, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    } catch (Exception e) {
                        log.error("写入 AI 对话历史失败（不影响本次生成结果），appId=" + appId, e);
                    }
                })
                .doOnError(error ->{
                    // 失败绝不能写成 AI 回复。
                    // 这里原来写的是 addChatMessage("Ai回复失败："+error.getMessage(), AI)，
                    log.error("appId=" + appId + " 的流式生成失败，不写入对话历史", error);
                });
    }

    /**
     * 解析并收集TokenStream数据
     */
    private String handleJsonMessageChunk(String chunk,StringBuilder chatHistoryStringBuilder,Set<String> seenToolIds){
        //解析Json
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        // type 为空或不在枚举里时必须提前返回：
        // switch 一个 null 会抛 NullPointerException（typeEnum.ordinal()），
        // 那会让整条 SSE 流直接失败，而这里本来只是"一条消息无法识别"而已。
        if (typeEnum == null) {
            log.warn("无法识别的流式消息类型: {}，原始内容前 200 字符: {}",
                    streamMessage.getType(),
                    StrUtil.maxLength(chunk, 200));
            return "";
        }
        switch (typeEnum){
            case AI_RESPONSE -> {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk,AiResponseMessage.class);
                String data = aiMessage.getData();
                //直接拼接响应
                chatHistoryStringBuilder.append(data);
                return data;
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk,ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                String toolName = toolRequestMessage.getName();
                //检查是否是第一次看到这个工具Id
                if (toolId!=null&&!seenToolIds.contains(toolId)){
                    //第一次调用这个这个工具，记录Id并完整返回工具信息
                    seenToolIds.add(toolId);
                    //根据工具名称获取工具实例
                    BaseTool tool = toolManager.getTool(toolName);
                    // 工具没注册时不能直接调，否则又是一个空指针把流打断
                    if (tool == null) {
                        log.warn("未注册的工具名称: {}，已跳过该条工具请求", toolName);
                        return "";
                    }
                    //返回格式化的工具调用信息
                    return tool.generateToolRequestResponse();
                }else{
                    return "";
                }
            }
            case TOOL_EXECUTED -> {
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                // 根据工具名称获取工具实例
                String toolName = toolExecutedMessage.getName();
                BaseTool tool = toolManager.getTool(toolName);
                if (tool == null) {
                    log.warn("未注册的工具名称: {}，已跳过该条工具执行结果", toolName);
                    return "";
                }
                // arguments 可能为空，JSONUtil.parseObj(null) 会抛异常
                JSONObject jsonObject = JSONUtil.parseObj(
                        StrUtil.blankToDefault(toolExecutedMessage.getArguments(), "{}"));
                String result = tool.generateToolExecutedResult(jsonObject);
                // 输出前端和要持久化的内容
                String output = String.format("\n\n%s\n\n", result);
                chatHistoryStringBuilder.append(output);
                return output;
            }
            default -> {
                log.error("不支持的消息类型: {}", typeEnum);
                return "";
            }
        }
    }
}
