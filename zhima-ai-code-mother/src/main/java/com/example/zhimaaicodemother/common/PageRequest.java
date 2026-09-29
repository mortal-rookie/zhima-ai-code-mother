package com.example.zhimaaicodemother.common;

import lombok.Data;
@Data
public class PageRequest {
    /**
     * 当前页号
     */
    private int pageNum=1;
    /**
    * 每页记录数
    */
    private int pageSize=10;
    /**
    * 排序字段
    */
    private String sortField;
    /**
    * 排序方式
    */
    private String sortOrder="descend";

}
