package com.example.zhimaaicodemother.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置
 * <p>
 * ⚠️ 安全红线：这里开启了 allowCredentials（允许跨域携带 Cookie），
 * 所以允许的来源**必须**是白名单，绝对不能用 "*"。
 * <p>
 * 用 allowedOriginPatterns("*") + allowCredentials(true) 的后果是：
 * 任意网站都能带着受害者的会话 Cookie 调用本服务并**读到响应内容**
 * （实测把 Origin 换成 https://evil.example.com，服务端会原样反射回来
 * 并附上 Access-Control-Allow-Credentials: true），等于同源策略完全失效：
 * 攻击者页面可以拉走用户的应用列表、聊天记录，管理员身份下还能拉走整张用户表。
 * <p>
 * 需要放行新域名时改配置（多个用逗号分隔）：
 * <pre>
 * code:
 *   cors:
 *     allowed-origins: https://your-domain.com,https://www.your-domain.com
 * </pre>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 允许跨域的来源白名单。
     * 默认只放行本地 Vite 开发服务器；线上部署时一定要用配置覆盖成自己的域名。
     */
    @Value("${code.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 只放行白名单里的来源（不要改成 "*"）
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // 只暴露前端真正要读的响应头：
                // 下载代码时要读 Content-Disposition 里的文件名，
                // 其余响应头没有暴露给 JS 的必要。
                .exposedHeaders("Content-Disposition");
    }
}
