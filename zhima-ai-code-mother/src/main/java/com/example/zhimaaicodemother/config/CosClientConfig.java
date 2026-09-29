package com.example.zhimaaicodemother.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * cos存储配置类
 * @author fanren
 */
@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosClientConfig {

    /**
     * 域名
     */
    private String host;

    /**
     * secretld
     */
    private String secretId;

    /**
     * 密钥
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 桶名
     */
    private String bucket;

    @Bean
    public COSClient cosClient(){
        //初始化用户身份信息
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        //设置Bucket区域
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        //创建COS客户端
        return new COSClient(cred, clientConfig);
    }
}
