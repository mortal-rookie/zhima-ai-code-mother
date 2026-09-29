package com.example.zhimaaicodemother.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.example.zhimaaicodemother.Exception.BusinessException;
import com.example.zhimaaicodemother.Exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

/**
 * 截图工具类
 */
@Slf4j
public class WebScreenshotUtils {

    private static ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    //全局静态初始化，避免重复初始化驱动程序
       private static final int DEFAULT_WIDTH =  1600;
       private static final int DEFAULT_HEIGHT = 900;

    private static WebDriver getWebDriver(){
        WebDriver webDriver = driverThreadLocal.get();
        if(webDriver==null){
            webDriver=initChromeDriver(DEFAULT_WIDTH,DEFAULT_HEIGHT);
            driverThreadLocal.set(webDriver);
        }
        return webDriver;
    }

    /**
     * 退出时销毁
     */
    public static void destroy(){
        WebDriver webDriver = driverThreadLocal.get();
        if(webDriver!=null){
            try{
                webDriver.quit();
            }catch (Exception e){
                log.error("销毁驱动时出现异常,message{}",e.getMessage());
            }finally {
                driverThreadLocal.remove();
            }
        }
    }

    /**生成网页截图
     *
     * @param webUrl 要截图的网址
     * @return 截图文件路径
     */
    public static String saveWebPageScreenshot(String webUrl){
        //非空校验
        if(StrUtil.isBlank(webUrl)){
            log.error("网页截图失败，url为空");
            return null;
        }
        //创建临时目录
        try{
            String rootPath = System.getProperty("user.dir")+"/tmp/screenshots/"+ UUID.randomUUID().toString().substring(0,8);
            FileUtil.mkdir(rootPath);
            //图片后缀
            final String IMAGE_SUFFIX = ".png";
            //原始图片保存路径
            String imageSavepath = rootPath+ File.separator+ RandomUtil.randomNumbers(5)+IMAGE_SUFFIX;
            //使用当前线程的webDriver
            WebDriver webDriver = getWebDriver();
            //访问页面
            webDriver.get(webUrl);
            //等待网页加载
            waitForPageLoad(webDriver);
            //截图
            byte[] screenshotBytes = ((TakesScreenshot)webDriver).getScreenshotAs(OutputType.BYTES);
            //保存原始图片
            saveImage(screenshotBytes,imageSavepath);
            log.info("原始截图保存成功：{}", imageSavepath);
            //压缩图片
            final String COMPRESS_SUFFIX = "_compressed.jpg";
            String compressedImagePath = rootPath+ File.separator+ RandomUtil.randomNumbers(5)+COMPRESS_SUFFIX;
            compressImage(imageSavepath,compressedImagePath);
            log.info("压缩图片保存成功：{}", compressedImagePath);
            //删除原始图片
            FileUtil.del(imageSavepath);
            return compressedImagePath;
        }catch (Exception e){
            log.error("网页截图失败：{}", webUrl, e);
            return null;
        }
    }
    /**
     * 初始化Chrome浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height){
        try{
            //自动管理ChromeDirver
            WebDriverManager.chromedriver()
                    .driverVersion("153.0.8010.53")
                    .driverRepositoryUrl(new URL("https://npmmirror.com/mirrors/chromedriver/"))
                    .setup();
            //配置Chrome选项
            ChromeOptions options = new ChromeOptions();
            //无头模式，让浏览器在后台运行
            options.addArguments("--headless");
            //禁用GPU,服务器、Docker 容器、环境通常没有独立 GPU
            options.addArguments("--disable-gpu");
            //禁用沙盒模式
            options.addArguments("--no-sandbox");
            //禁用shm使用
            options.addArguments("--disable-dev-shm-usage");
            //设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d",width,height));
            //禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        }catch (Exception e){
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    /**
     * 保存图片到文件
     *
     * @param imageBytes
     * @param imagePath
     */
    private static void saveImage(byte[] imageBytes,String imagePath){
        try{
            FileUtil.writeBytes(imageBytes,imagePath);
        }catch (Exception e){
            log.error("图片保存失败：{}",imagePath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存文件失败");
        }
    }

    /**
     * 压缩图片
     *
     * @param originImagePath
     * @param compressedImagePath
     */
    private static void compressImage(String originImagePath, String compressedImagePath) {
        // 压缩图片质量（0.1 = 10% 质量）
        final float COMPRESSION_QUALITY = 0.3f;
        try {
            ImgUtil.compress(
                    FileUtil.file(originImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESSION_QUALITY
            );
        } catch (Exception e) {
            log.error("压缩图片失败：{} -> {}", originImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待页面加载完成
     * @param webDriver
     */
    private static  void waitForPageLoad(WebDriver webDriver){
        try{
            //创建等待页面加载对象
            WebDriverWait wait = new WebDriverWait(webDriver,Duration.ofSeconds(10));
            wait.until(driver ->((JavascriptExecutor) driver)
                    .executeScript("return document.readyState")
                    .equals("complete")
            );
            //额外添加一段时间
            Thread.sleep(2000);
            log.info("页面加载完成");
        }catch (Exception e){
            log.error("等待页面加载时出现异常，继续执行截图",e);
        }
    }
}
