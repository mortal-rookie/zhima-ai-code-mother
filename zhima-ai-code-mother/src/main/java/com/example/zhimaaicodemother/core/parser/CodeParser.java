package com.example.zhimaaicodemother.core.parser;

public interface CodeParser<T> {
    /**
     * 解析代码内容
     * @param codeContent 原始代码内容
     * @return 解析后的对象
     */
    T parseCode(String codeContent);
}
