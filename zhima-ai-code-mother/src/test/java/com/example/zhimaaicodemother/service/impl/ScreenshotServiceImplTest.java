package com.example.zhimaaicodemother.service.impl;

import com.example.zhimaaicodemother.service.ScreenshotService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ScreenshotServiceImplTest {
    @Resource
    private ScreenshotService screenshotService;

    @Test
    void generateAndUploadScreenshot() {
        screenshotService.generateAndUploadScreenshot("http://www.baidu.com");
    }
}