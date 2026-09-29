package com.example.zhimaaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 多文件代码结果
 */
@Description("多文件代码结果")
@Data
public class MultiFileCodeResult {
    /**
     * html代码
     */
    @Description("html代码")
    private String htmlCode;
    /**
     * css代码
     */
    @Description("css代码")
    private String cssCode;
    /**
     * js 代码
     */
    @Description("js代码")
    private String jsCode;
    /**
     * 描述
     */
    @Description("描述")
    private String description;
}
