package com.example.zhimaaicodemother.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用部署请求
 */
@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用id
     */
    Long id;

    private static final Long serialVersionUID=1L;
}
