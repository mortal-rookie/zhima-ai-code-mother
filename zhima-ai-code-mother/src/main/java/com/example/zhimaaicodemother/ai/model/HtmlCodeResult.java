package com.example.zhimaaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * html代码结果
 */
@Description("html代码结果")
@Data
public class HtmlCodeResult {
    /**
     * html代码
     */
    @Description("html代码")
    private String htmlCode;
    /**
     * 描述
     */
    @Description("描述")
    private String description;
}
