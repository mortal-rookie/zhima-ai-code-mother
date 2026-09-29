package com.example.zhimaaicodemother.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {
    private String host;

    private int port;

    private String password;

    private long ttl;

    /** Redis 内置的默认 ACL 用户名，未额外创建用户时用它做认证 */
    private static final String DEFAULT_REDIS_USER = "default";

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore(){

        return RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .user(DEFAULT_REDIS_USER)
                .password(password)
                .ttl(ttl)
                .build();
    }
}
