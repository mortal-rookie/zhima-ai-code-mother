package com.example.zhimaaicodemother.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 脱敏后的用户信息
 * @author fanren
 */
@Data
public class UserVO implements Serializable {
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

    private static final long servialVersionUID = 1L;
}
