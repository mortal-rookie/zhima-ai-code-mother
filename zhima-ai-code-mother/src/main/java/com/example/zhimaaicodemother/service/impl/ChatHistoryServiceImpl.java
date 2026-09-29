package com.example.zhimaaicodemother.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.example.zhimaaicodemother.Exception.ErrorCode;
import com.example.zhimaaicodemother.Exception.ThrowUtils;
import com.example.zhimaaicodemother.constant.UserConstant;
import com.example.zhimaaicodemother.mapper.ChatHistoryMapper;
import com.example.zhimaaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.example.zhimaaicodemother.model.entity.App;
import com.example.zhimaaicodemother.model.entity.ChatHistory;
import com.example.zhimaaicodemother.model.entity.User;
import com.example.zhimaaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.example.zhimaaicodemother.service.AppService;
import com.example.zhimaaicodemother.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper,ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy//a依赖b，b依赖a，延迟注入
    private AppService appService;

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        // 基础校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型");
        // 插入数据库
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }

    /**
     * 分页查询某 APP 的对话记录
     *
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param loginUser
     * @return
     */
    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser){
        //校验参数
        ThrowUtils.throwIf(appId==null||appId<=0, ErrorCode.PARAMS_ERROR,"应用id不能为空");
        ThrowUtils.throwIf(pageSize<=0 || pageSize>50,ErrorCode.PARAMS_ERROR,"页面大小需要在0~50之间");
        // lastCreateTime 是游标参数：首屏查询时为空表示「从最新一条开始」，不能校验非空
        ThrowUtils.throwIf(loginUser==null,ErrorCode.PARAMS_ERROR,"登录用户为空");
        //权限校验，只有管理员和应用创建者
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.PARAMS_ERROR,"应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        // 管理员「或」创建者都可查看：只有两者都不是才拒绝
        ThrowUtils.throwIf(!isAdmin&&!isCreator,ErrorCode.NO_AUTH_ERROR,"无权限查看");
        //构造查询条件
        ChatHistoryQueryRequest chatHistoryQueryRequest = new ChatHistoryQueryRequest();
        chatHistoryQueryRequest.setAppId(appId);
        chatHistoryQueryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = getQueryWrapper(chatHistoryQueryRequest);
        //查询数据
        return this.page(Page.of(1,pageSize),queryWrapper);
    }

    /**
     * 加载对话历史到内存
     *
     * @param appId
     * @param chatMemory
     * @param maxCount   最多加载多少条
     * @return 加载成功的条数
     */
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount){
        try{
            // 必须先清空记忆，再判断"库里有没有历史"。
            // 原来的顺序是先 return 再 clear：数据库里没有历史时直接返回，
            // Redis 里那份旧记忆就被原样留了下来继续使用。
            // 而它很可能已经是坏的——工具调用轮次超限时，langchain4j 会把
            // assistant(tool_calls) 写进记忆之后才抛异常，对应的 tool 响应永远补不上，
            // 这条"有 tool_calls 却没有 tool 响应"的孤儿消息再发给 DeepSeek 会被直接拒绝：
            //   An assistant message with 'tool_calls' must be followed by tool messages
            //   responding to each 'tool_call_id'.
            // 数据库才是唯一可信来源：库里没有历史，记忆就必须是空的。
            chatMemory.clear();
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId,appId)
                    .orderBy(ChatHistory::getCreateTime,false)
                    .limit(1,maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if(CollUtil.isEmpty(historyList)){
                log.info("appid：{} 没有历史对话，记忆已重置为空", appId);
                return 0;
            }
            //反转列表
            historyList = historyList.reversed();
            //按照时间顺序将消息添加到记忆中
            int loadedCount = 0;
            for(ChatHistory chatHistory : historyList){
                if(ChatHistoryMessageTypeEnum.USER.getValue().equals(chatHistory.getMessageType())){
                    chatMemory.add(UserMessage.from(chatHistory.getMessage()));
                }else if(ChatHistoryMessageTypeEnum.AI.getValue().equals(chatHistory.getMessageType())){
                    // 两处都必须是「类型判类型、内容取内容」：
                    // 原来这里是 equals(getMessage()) 比内容，永远不成立，
                    // 于是历史里的 AI 回复一条都不会进记忆（模型看不到自己写过什么，
                    // 后续「修改某某页面」的需求必然改错文件）；
                    // 而下一行原本取的是 getMessageType()，一旦条件成立会把每条 AI 回复
                    // 都变成字面量 "ai"。两个错误互相掩盖，必须一起改。
                    chatMemory.add(AiMessage.from(chatHistory.getMessage()));
                }
                loadedCount++;
            }
            log.info("成功为 appid：{} 加载了 {} 条对话历史到内存",appId,loadedCount);
            return loadedCount;
        }catch (Exception e){
            log.error("加载对话历史到内存失败，appId：{},error:{}",appId,e.getMessage());
            return 0;
        }
    }
    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        // 必须逐个判空：MyBatis-Flex 的 eq / like 不会因为值为 null 就自动跳过条件，
        // 而是原样拼成 `id = ?` / `message like ?`，这类条件永远不成立。
        // 之前这里是无条件链式调用，接口实际执行的是
        //   SELECT COUNT(*) FROM chat_history WHERE (id = ? AND appId = ?) ...
        // 于是数据库里明明有 12 条历史，返回的却是 0 条，聊天页永远加载不出对话记录。
        //
        // 注意 id 在 ChatHistoryQueryRequest 里是基本类型 long（不是包装类型 Long），
        // 没传时得到的是 0 而不是 null，所以这里必须按 > 0 判断，
        // 用 != null 判空完全拦不住（0 会被当成合法 id 拼进 SQL）。
        if (id > 0) {
            queryWrapper.eq("id", id);
        }
        if (StrUtil.isNotBlank(message)) {
            queryWrapper.like("message", message);
        }
        if (StrUtil.isNotBlank(messageType)) {
            queryWrapper.eq("messageType", messageType);
        }
        if (appId != null) {
            queryWrapper.eq("appId", appId);
        }
        if (userId != null) {
            queryWrapper.eq("userId", userId);
        }
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }
}
