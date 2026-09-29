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

/**
 * 文件删除工具
 * 支持Ai通过调用工具的方式删除文件
 */
@Slf4j
@Component
public class FileDeleteTool extends BaseTool{

    @Tool("删除指定路径的文件")
    public String deleteFile(@P("删除指定路径的文件") String relativeFilePath, @ToolMemoryId long appId){
        try{
            // 只允许删本应用项目目录内的文件，绝对路径和 .. 跳转一律拒绝。
            // 顺带修掉一个老 bug：这里原来拼的是 "vue_project"+appId（少了下划线），
            // 指向的是一个根本不存在的目录，所以删除功能一直是失效的。
            Path path = resolveSafePath(appId, relativeFilePath);
            if (path == null) {
                log.warn("拒绝越界的文件删除: appId={}, path={}", appId, relativeFilePath);
                return illegalPathMessage(relativeFilePath);
            }
            if(!Files.exists(path)){
                return "警告，文件不存在，无法删除-"+relativeFilePath;
            }
            if(!Files.isRegularFile(path)){
                return "错误，指定路径不是文件，无法删除-"+relativeFilePath;
            }
            //安全检查，避免删除重要文件
            String filename = path.getFileName().toString();
            if(isImportantFile(filename)){
                return "错误，不允许删除重要文件-"+filename;
            }
            Files.delete(path);
            log.info("成功删除文件: {}", path.toAbsolutePath());
            return "文件删除成功: " + relativeFilePath;
        }catch (IOException e) {
            String errorMessage = "删除文件失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    /**
     * 判断是否是重要文件，不允许删除
     */
    private boolean isImportantFile(String filename){
        String[] importantFiles = {
                "package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",
                "vite.config.js", "vite.config.ts", "vue.config.js",
                "tsconfig.json", "tsconfig.app.json", "tsconfig.node.json",
                "index.html", "main.js", "main.ts", "App.vue", ".gitignore", "README.md"
        };
        for(String important: importantFiles){
            if(important.equalsIgnoreCase(filename)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String getToolName() {
        return "deleteFile";
    }

    @Override
    public String getDisplayName() {
        return "删除文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        return String.format(" [工具调用] %s %s", getDisplayName(), relativeFilePath);
    }
}
