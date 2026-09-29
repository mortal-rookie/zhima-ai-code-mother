package com.example.zhimaaicodemother.common;

import lombok.Data;

import java.io.Serializable;
@Data
public class DeleteRequest implements Serializable {
    /**
     * id
     */
    private Long id;//Long可以存放null，防止误删
    private static final long serialVersionUID = 1L;
}
