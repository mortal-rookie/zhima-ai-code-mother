package com.example.zhimaaicodemother.ai;

import dev.langchain4j.service.SystemMessage;

/**
 * AI代码名称生成服务
 */
public interface AiCodeGenNameService {

    /**
     * 根据用户输入生成代码名称
     * @param userPrompt
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-name-system-prompt.txt")
    String generateName(String userPrompt);
}
