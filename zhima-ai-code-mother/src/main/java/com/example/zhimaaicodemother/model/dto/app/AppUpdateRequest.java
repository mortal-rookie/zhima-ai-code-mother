package com.example.zhimaaicodemother.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新应用请求类
 * @author fanren
 */
@Data
public class AppUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;
    /**
     * 应用名称
     */
    private String appName;

    private static final Long serialVersionUID =1L;
}
