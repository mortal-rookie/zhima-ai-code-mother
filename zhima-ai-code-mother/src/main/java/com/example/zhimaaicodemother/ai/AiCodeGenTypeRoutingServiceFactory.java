package com.example.zhimaaicodemother.ai;

import com.example.zhimaaicodemother.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ai代码生成类型路由服务工厂
 * @author fanren
 */
@Slf4j
@Configuration//@Bean方法会被Spring代理，多次调用这个方法，返回容器里面的同一个单例对象，不会重复new
public class AiCodeGenTypeRoutingServiceFactory {

    /**
     * 创建Ai代码生成类型路由服务器实例
     */
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService(){
        ChatModel chatModel = SpringContextUtil.getBean("routingChatModelPrototype",ChatModel.class);
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }

    /**
     * 默认提供一个 Bean
     */
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService(){
        return createAiCodeGenTypeRoutingService();
    }
}
