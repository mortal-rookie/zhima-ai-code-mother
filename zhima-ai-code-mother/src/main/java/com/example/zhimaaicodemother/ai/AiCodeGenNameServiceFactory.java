package com.example.zhimaaicodemother.ai;

import com.example.zhimaaicodemother.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ai 生成名称服务工厂类
 */
@Slf4j
@Configuration
public class AiCodeGenNameServiceFactory {

    // 生成名称服务实例
    @Bean
    public AiCodeGenNameService aiCodeGenNameService(@Qualifier("nameChatModel") ChatModel chatModel){
        return AiServices.builder(AiCodeGenNameService.class)
                .chatModel(chatModel)
                .build();
    }

}
