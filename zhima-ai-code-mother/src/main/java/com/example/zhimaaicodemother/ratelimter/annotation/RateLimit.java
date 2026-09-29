package com.example.zhimaaicodemother.ratelimter.annotation;

import com.example.zhimaaicodemother.ratelimter.enums.RateLimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//表示只能在函数上使用
@Target({ElementType.METHOD})
//表示运行时保留，可以通过反射获取
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流key前缀
     */
    String key() default "";

    /**
     * 每个时间窗口允许的请求数
     */
    int rate() default 10;

    /**
     * 时间窗口（秒）
     */
    int rateInterval() default 1;

    /**
     * 限流类型
     */
    RateLimitType limitType() default RateLimitType.USER;

    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}