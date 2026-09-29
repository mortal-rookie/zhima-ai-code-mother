package com.example.zhimaaicodemother.core.parser;

import com.example.zhimaaicodemother.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML 单文件代码解析器
 * @author fanren
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {
    //创建正则对象，模式为忽略大小写
    private static final Pattern HtML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```",Pattern.CASE_INSENSITIVE);

    @Override
    public HtmlCodeResult parseCode(String codeContent){
        HtmlCodeResult result = new HtmlCodeResult();
        //提取HTML代码
        String htmlCode = extractHtmlCode(codeContent);
        if(htmlCode != null&&!htmlCode.trim().isEmpty()){
            result.setHtmlCode(htmlCode.trim());//trim()去除字符串前后空格
        }else{
            //如果找不到代码块，将整个内容作为HTML
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }
    /**
     * 提取HTml代码内容
     * @param content 原始内容
     * @return HTML代码
     */
    private String extractHtmlCode(String content){
        Matcher matcher = HtML_CODE_PATTERN.matcher(content);
        if(matcher.find()){
            return matcher.group(1);
        }
        return null;
    }
}
