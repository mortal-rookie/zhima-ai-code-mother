package com.example.zhimaaicodemother.constant;

/**
 *用户常量
 */
public interface UserConstant {
    /**
     * 用户登录态键
     */
    public static final String USER_LOGIN_STATE = "USER_LOGIN";
    /**
     * 默认角色
     */
    public static final String DEFAULT_ROLE = "user";
    /**
     * 管理员角色
     */
    public static final String ADMIN_ROLE = "admin";
    /**
     * 普通用户每日免费生成次数（管理员不受此限制）
     */
    public static final int DEFAULT_DAILY_GEN_QUOTA = 2;
}
