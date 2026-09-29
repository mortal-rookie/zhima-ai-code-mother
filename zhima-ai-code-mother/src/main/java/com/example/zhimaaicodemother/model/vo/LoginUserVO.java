package com.example.zhimaaicodemother.model.vo;

import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 脱敏后的用户登录信息
 */
@Data
public class LoginUserVO {
    /**
    * 用户ID
    */
    private Long id;
    /**
     * 账号
     */
    private String userAccount;
    /**
     *用户昵称
     */
    private String userName;
    /**
    * 用户头像
    */
    private String userAvatar;
    /**
    * 用户角色 user/admin
    */
    private String userRole;
    /**
     * 用户简介
     */
    private String userProfile;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
    * 更新时间
    */
    private LocalDateTime updateTime;

    /**
     * 今日已使用次数
     */
    private Integer dailyGenUsed;

    /**
     * 上次更新时间
     */
    private LocalDate genResetData;

    private static final long servialVersionUID = 1L;
}
