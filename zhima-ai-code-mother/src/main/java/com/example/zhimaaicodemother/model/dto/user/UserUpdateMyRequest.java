package com.example.zhimaaicodemother.model.dto.user;

import lombok.Data;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户更新自己的请求
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

}
