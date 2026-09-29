package com.example.zhimaaicodemother.config;

import com.example.zhimaaicodemother.constant.AppConstant;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * 静态资源映射
 *
 * 1）生成目录 → /static/**
 *    前端拼出来的预览地址有两种形状：
 *      Vue 项目：{API_BASE_URL}/static/vue_project_{appId}/dist/index.html
 *      HTML / 多文件：{API_BASE_URL}/static/{type}_{appId}/          ← 目录形式，不带文件名
 *    在这个配置存在之前，项目里没有任何 /static/** 的处理器，
 *    所以无论代码生成得多成功，预览都只会是 404 空白页。
 *
 * 2）部署目录 → /deploy/**
 *    部署功能会把成品复制到 tmp/code_deploy/{deployKey}/，
 *    并返回 {code.deploy-host}/{deployKey}/ 作为访问地址。
 *    默认的 deploy-host 是 http://localhost（80 端口），本地既没服务监听 80、
 *    也没有 nginx 托管该目录，所以部署会"成功"但地址打不开。
 *
 * @author fanren
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 生成目录：前端 iframe 预览 /static/**
        registry.addResourceHandler("/static/**")
                .addResourceLocations(toFileLocation(AppConstant.CODE_OUTPUT_ROOT_DIR))
                // 关闭资源链缓存，保证生成完新文件后刷新预览能立刻看到
                .resourceChain(false)
                .addResolver(new IndexFallbackResourceResolver());

        // 部署目录：部署后对外访问 /deploy/{deployKey}/
        // 后端返回的部署地址由 code.deploy-host 决定，默认是 http://localhost（80 端口），
        // 而本地根本没有东西监听 80 端口，也没有 nginx 托管 tmp/code_deploy，
        // 所以部署会"成功"但地址打不开。这里把部署目录也交给后端来托管。
        // 注意两个 handler 必须各用一个 resolver 实例：
        // ResourceChainRegistration 会把当前 handler 的 locations 灌进 PathResourceResolver，
        // 共用实例会让后一个把前一个的 allowedLocations 覆盖掉，导致前一个 handler 全部 403/404。
        registry.addResourceHandler("/deploy/**")
                .addResourceLocations(toFileLocation(AppConstant.CODE_DEPLOY_ROOT_DIR))
                .resourceChain(false)
                .addResolver(new IndexFallbackResourceResolver());
    }

    /**
     * 把本地目录转成 Spring 资源处理器能识别的 file: 形式 URI。
     * 直接把 "file:" 和 Windows 绝对路径拼起来会因为盘符里的冒号被当成非法 URL 而解析失败，
     * 所以统一走 Paths.toUri()，并保证结尾有斜杠（否则会被当成文件而不是目录）。
     */
    private String toFileLocation(String dir) {
        String location = Paths.get(dir).toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }

    /**
     * 目录回退到 index.html 的解析器。
     *
     * HTML / 多文件模式的预览地址是「目录」而不是具体文件，
     * 而 Spring 默认的 PathResourceResolver 不会做目录索引，
     * 请求目录只会拿到 404（实测 /static/html_xxx/ 就是 404，
     * 而 /static/html_xxx/index.html 是 200）。
     * 这里在默认解析失败或解析结果是目录时，自动回退到该目录下的 index.html。
     */
    private static class IndexFallbackResourceResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource resource = super.getResource(resourcePath, location);
            if (isReadableFile(resource)) {
                return resource;
            }
            // 目录，或者路径本身就以 / 结尾（甚至就是空串，即 /static/ 根）：
            // 一律尝试该目录下的 index.html
            String indexPath = resourcePath.isEmpty() || resourcePath.endsWith("/")
                    ? resourcePath + "index.html"
                    : resourcePath + "/index.html";
            Resource indexResource = super.getResource(indexPath, location);
            return isReadableFile(indexResource) ? indexResource : null;
        }

        private boolean isReadableFile(Resource resource) {
            if (resource == null || !resource.isReadable()) {
                return false;
            }
            try {
                // 目录也是 readable 的，但它不能直接当响应体写出去，必须走 index.html
                return !resource.getFile().isDirectory();
            } catch (Exception e) {
                // 非 file 协议的资源（例如打进 jar 的）一律按文件处理
                return true;
            }
        }
    }
}
