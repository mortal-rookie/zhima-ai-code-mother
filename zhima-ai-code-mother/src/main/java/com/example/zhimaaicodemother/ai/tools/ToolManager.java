package com.example.zhimaaicodemother.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具管理工具
 * 统一管理所有工具，提供根据名称获取工具的方法
 */
@Slf4j
@Component
public class ToolManager {

    /**
     * 工具名称到工具实例的映射
     */
    private final Map<String,BaseTool> toolMap = new HashMap<>();

    /**
     * 自动注入所有工具
     */
    @Resource
    private BaseTool[] tools;

    /**
     * 初始化工具映射
     *
     * 必须加 @PostConstruct：本方法原来没有任何地方主动调用，
     * 导致 toolMap 始终为空、getTool() 永远返回 null，
     * 进而在 JsonMessageStreamHandler 里触发
     * 「Cannot invoke BaseTool.generateToolRequestResponse() because "tool" is null」。
     * 加上该注解后，Spring 会在依赖注入（即 tools 数组就绪）之后自动执行一次。
     */
    @PostConstruct
    public void initTools(){
        if (tools == null || tools.length == 0) {
            log.warn("未注入任何 BaseTool 实现，工具映射为空");
            return;
        }
        for(BaseTool tool : tools){
            toolMap.put(tool.getToolName(),tool);
            log.info("注册工具：{}-{}",tool.getToolName(),tool.getDisplayName());
        }
        log.info("工具管理初始化完成，共注册工具数量：{}",toolMap.size());
    }

    /**
     * 根据工具名称获取工具实例
     *
     * 兜底：万一初始化没生效，这里按需补建一次，避免再抛空指针。
     */
    public BaseTool getTool(String toolName){
        if (toolMap.isEmpty() && tools != null && tools.length > 0) {
            log.warn("工具映射为空，触发一次惰性初始化");
            initTools();
        }
        return toolMap.get(toolName);
    }

    /**
     * 获取已注册的工具集合
     */
    public BaseTool[] getAllTools(){
        return tools;
    }
}
