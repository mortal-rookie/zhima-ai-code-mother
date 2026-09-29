package com.example.zhimaaicodemother.core.parser;

import com.example.zhimaaicodemother.Exception.BusinessException;
import com.example.zhimaaicodemother.Exception.ErrorCode;
import com.example.zhimaaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 代码解析执行器
 * 根据代码生成类型执行相应解析逻辑
 *
 * @author fanren
 */
public class CodePaserExecutor {
    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 执行代码解析
     *
     * @param codeContent 代码内容
     * @param codeGenTypeEnum 代码生成类型
     * @return 解析结果（HtmlCodeResult 或 MultiFileCodeResult）
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum){
        return switch(codeGenTypeEnum){
            case HTML-> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE-> multiFileCodeParser.parseCode(codeContent);
            default-> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持代码生成类型");
        };
    }
}
