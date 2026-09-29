package com.example.zhimaaicodemother.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 代码生成类型枚举
 */
@Getter
public enum CodeGenTypeEnum {

    HTML("原生HTML模式","html"),
    MULTI_FILE("原生多文件模式","multi_file"),
    VUE_PROJECT("Vue工程模式","vue_project");

    private final String text;
    private final String value;

    CodeGenTypeEnum(String text,String value){
        this.text=text;
        this.value=value;
    }

    /**
     * 根据value获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static CodeGenTypeEnum getEnumByValue(String value){
        if(ObjUtil.isEmpty(value)){
            return null;
        }
        //CodeGenTypeEnum.values()：返回枚举组成的数组
        for(CodeGenTypeEnum anEnum : CodeGenTypeEnum.values()){
            if(anEnum.value.equals(value)){
                return anEnum;
            }
        }
        return null;
    }
}
