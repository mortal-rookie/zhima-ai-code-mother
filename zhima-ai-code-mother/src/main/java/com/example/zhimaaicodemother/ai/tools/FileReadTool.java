package com.example.zhimaaicodemother.ai.tools;

import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件读取工具
 * 支持 AI 通过工具调用的方式读取文件内容
 */
@Slf4j
@Component
public class FileReadTool extends BaseTool {

    /**
     * 记录每个文件上次被读取时的最后修改时间，用于拦截"反复读同一个文件"
     * key 是文件绝对路径，value 是当时的 lastModifiedTime。
     */
    private final Map<String, Long> lastReadStamp = new ConcurrentHashMap<>();

    @Tool("读取指定路径的文件内容")
    public String readFile(
            @P("文件的相对路径")
            String relativeFilePath,
            @ToolMemoryId Long appId
    ) {
        try {
            // 只允许读本应用项目目录内的文件，绝对路径和 .. 跳转一律拒绝
            Path path = resolveSafePath(appId, relativeFilePath);
            if (path == null) {
                log.warn("拒绝越界的文件读取: appId={}, path={}", appId, relativeFilePath);
                return illegalPathMessage(relativeFilePath);
            }
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return "错误：文件不存在或不是文件 - " + relativeFilePath;
            }
            // 同一个文件、内容没有变化时，第二次读不再返回全文。
            // 模型在"自我校验"阶段会一轮并发重读十几个文件，而每个文件的全文动辄上万字符；
            // 几轮下来就把对话记忆窗口（maxMessages=20）彻底挤爆，
            // 连它自己刚写过的内容都被挤出去，于是越读越糊涂，
            // 一路空转到工具调用轮次上限（实测 50 轮里约 30 轮全是这种无效重读）。
            long stamp = Files.getLastModifiedTime(path).toMillis();
            Long previous = lastReadStamp.put(path.toAbsolutePath().toString(), stamp);
            if (previous != null && previous == stamp) {
                log.info("拦截重复读取（内容未变化）: {}", relativeFilePath);
                return "该文件自你上次读取后没有任何变化（" + relativeFilePath
                        + "），内容与上次完全相同，这里不再重复返回。"
                        + "请基于你已经看到的内容继续；若所有文件都已写完，请调用 exit 工具结束。";
            }
            return Files.readString(path);
        } catch (IOException e) {
            String errorMessage = "读取文件失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "readFile";
    }

    @Override
    public String getDisplayName() {
        return "读取文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        return String.format("[工具调用] %s %s", getDisplayName(), relativeFilePath);
    }
}
