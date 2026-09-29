package com.example.zhimaaicodemother.monitor;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * AI 模型监听器
 */
@Component
public class AiModelMonitorListener implements ChatModelListener {

    // 用于存储请求开始时间的键
    private static final String REQUEST_START_TIME_KEY = "request_start_time";
    // 用于监控上下文传递（因为请求和响应事件的触发不是同一个线程）
    private static final String MONITOR_CONTEXT_KEY = "monitor_context";

    @Resource
    private AiModelMetricsCollector aiModelMetricsCollector;

    /** 上下文缺失时用于打点的占位值，避免因为空指针丢掉整条监控数据 */
    private static final String UNKNOWN = "unknown";

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        // 获取当前时间戳
        requestContext.attributes().put(REQUEST_START_TIME_KEY, Instant.now());
        // 从监控上下文中获取信息；
        // 注意：MonitorContextHolder 的上下文是由跟踪切面写入的，
        // 若本次调用没有经过该切面（例如定时任务、内部直调），这里会是 null。
        MonitorContext monitorContext = MonitorContextHolder.getContext();
        String userId = resolveUserId(monitorContext);
        String appId = resolveAppId(monitorContext);
        // 只在上下文存在时才写入 attributes。
        // langchain4j 的 attributes 是 ConcurrentHashMap，不接受 null 值，
        // 直接 put(key, null) 会在 putVal 里抛 NullPointerException。
        // 存不进去也没关系：onResponse 取不到该键时会退化成 unknown 打点。
        if (monitorContext != null) {
            requestContext.attributes().put(MONITOR_CONTEXT_KEY, monitorContext);
        }
        // 获取模型名称
        String modelName = requestContext.chatRequest().modelName();
        // 记录请求指标
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "started");
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        // 从属性中获取监控信息（由 onRequest 方法存储）
        Map<Object, Object> attributes = responseContext.attributes();
        // 属性里可能没有该键（onRequest 未执行或异常），同样需要判空
        MonitorContext context = (MonitorContext) attributes.get(MONITOR_CONTEXT_KEY);
        String userId = resolveUserId(context);
        String appId = resolveAppId(context);
        // 获取模型名称
        String modelName = responseContext.chatResponse().modelName();
        // 记录成功请求
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "success");
        // 记录响应时间
        recordResponseTime(attributes, userId, appId, modelName);
        // 记录 Token 使用情况
        recordTokenUsage(responseContext, userId, appId, modelName);
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        // 从监控上下文中获取信息（同上，可能为 null）
        MonitorContext context = MonitorContextHolder.getContext();
        String userId = resolveUserId(context);
        String appId = resolveAppId(context);
        // 获取模型名称和错误类型
        String modelName = errorContext.chatRequest().modelName();
        String errorMessage = errorContext.error().getMessage();
        // 记录失败请求
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "error");
        aiModelMetricsCollector.recordError(userId, appId, modelName, errorMessage);
        // 记录响应时间（即使是错误响应）
        Map<Object, Object> attributes = errorContext.attributes();
        recordResponseTime(attributes, userId, appId, modelName);
    }

    /** 上下文或其 userId 为空时退化为 unknown，保证指标仍然能打点 */
    private String resolveUserId(MonitorContext context) {
        return (context == null || context.getUserId() == null) ? UNKNOWN : context.getUserId();
    }

    /** 同上，针对 appId */
    private String resolveAppId(MonitorContext context) {
        return (context == null || context.getAppId() == null) ? UNKNOWN : context.getAppId();
    }

    /**
     * 记录响应时间
     */
    private void recordResponseTime(Map<Object, Object> attributes, String userId, String appId, String modelName) {
        Instant startTime = (Instant) attributes.get(REQUEST_START_TIME_KEY);
        // 没有起始时间就无法计算耗时，直接跳过，避免 Duration.between(null, ...) 抛异常
        if (startTime == null) {
            return;
        }
        Duration responseTime = Duration.between(startTime, Instant.now());
        aiModelMetricsCollector.recordResponseTime(userId, appId, modelName, responseTime);
    }

    /**
     * 记录Token使用情况
     */
    private void recordTokenUsage(ChatModelResponseContext responseContext, String userId, String appId, String modelName) {
        TokenUsage tokenUsage = responseContext.chatResponse().metadata().tokenUsage();
        if (tokenUsage != null) {
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "input", tokenUsage.inputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "output", tokenUsage.outputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "total", tokenUsage.totalTokenCount());
        }
    }
}

