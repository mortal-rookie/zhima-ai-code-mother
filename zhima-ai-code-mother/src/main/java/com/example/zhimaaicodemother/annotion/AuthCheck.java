package com.example.zhimaaicodemother.annotion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//限制注解写在那个地方上，这里限制在方法上
@Retention(RetentionPolicy.RUNTIME)//注解的存活周期，这里是运行时依然保留注解信息

public @interface AuthCheck {
    /**
     * 必须有某个角色
     */
    String mustRole() default "";
}
