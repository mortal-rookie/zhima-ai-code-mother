package com.example.zhimaaicodemother.ai;

import com.example.zhimaaicodemother.ai.model.HtmlCodeResult;
import com.example.zhimaaicodemother.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AiCodeGeneratorServiceTest {
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result=aiCodeGeneratorService.generateHtmlCode("做一个程序员凡人的博客网站，不超过20行");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult result1=aiCodeGeneratorService.generateMultiFileCode("做一个程序员凡人的留言板，不超过20行");
        Assertions.assertNotNull(result1);
    }
}