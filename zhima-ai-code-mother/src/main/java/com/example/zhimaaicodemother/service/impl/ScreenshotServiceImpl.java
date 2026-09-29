package com.example.zhimaaicodemother.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.example.zhimaaicodemother.Exception.ErrorCode;
import com.example.zhimaaicodemother.Exception.ThrowUtils;
import com.example.zhimaaicodemother.manager.CosManager;
import com.example.zhimaaicodemother.service.ScreenshotService;
import com.example.zhimaaicodemother.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR,"网页Url不能为空");
        log.info("开始生成网页截图，URL:{}",webUrl);
        //生成本地截图
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath),ErrorCode.OPERATION_ERROR,"本地截图生成失败");
        try{
            //上传截图到Cos
            String cosUrl = uploadScreenshotToCos(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl),ErrorCode.OPERATION_ERROR,"截图上传对象失败");
            log.info("截图上传成功，URL:{}->{}",webUrl,cosUrl);
            return cosUrl;
        }finally {
            //清理本地文件
            cleanupLocalFile(localScreenshotPath);
        }
    }

    /**
     * 上传截图到对象存储
     *
     * @param localScreenshotPath 本地截图路径
     * @return Cos截图URL
     */
    private String uploadScreenshotToCos(String localScreenshotPath){
        if(StrUtil.isBlank(localScreenshotPath)){
            return null;
        }
        File screenshotFile = new File(localScreenshotPath);
        if(!screenshotFile.exists()){
            log.error("截图文件不存在：{}",localScreenshotPath);
            return null;
        }
        //生成cos对象键
        String fileName = UUID.randomUUID().toString().substring(0,8)+"_compressed.jpg";
        String cosKey = generateScreenshotKey(fileName);
        return cosManager.uploadFile(cosKey,screenshotFile);
    }

    /**
     * 生成截图的对象存储键
     * 格式：/screenshots/2026/09/26/filename.jpg
     */
    private String generateScreenshotKey(String fileName){
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s",datePath,fileName);
    }

    /**
     * 清理本地截图文件
     * @param localFilePath
     */
    private void cleanupLocalFile(String localFilePath){
        File localFile = new File(localFilePath);
        if(localFile.exists()){
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("本地截图文件已清理：{}",localFilePath);
        }
    }
}
