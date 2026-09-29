package com.example.zhimaaicodemother.core.builder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * 构建vue项目
 */
@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * 构建过程的标准输出/错误日志文件名（保存在被构建的项目根目录下）
     */
    private static final String BUILD_LOG_FILE_NAME = "build-output.log";

    /**
     * 构建 Vue 项目
     *
     * @param projectPath 项目根目录路径
     * @return 是否构建成功
     */
    public boolean buildProject(String projectPath) {
        File projectdir = new File(projectPath);
        if(!projectdir.exists()||!projectdir.isDirectory()){
            log.error("项目不存在：{}",projectPath);
            return false;
        }
        //检查是否有package.json文件
        File packageJsonFile = new File(projectdir,"package.json");
        if(!packageJsonFile.exists()){
            log.error("项目目录中缺少 package.json 文件:{}",projectPath);
            return false;
        }
        log.info("开始构建 Vue 项目：{}",projectPath);
        // 执行 npm install
        if (!executeNpmInstall(projectdir)) {
            log.error("npm install 执行失败：{}", projectPath);
            return false;
        }
        // 执行 npm run build
        if (!executeNpmBuild(projectdir)) {
            log.error("npm run build 执行失败：{}", projectPath);
            return false;
        }
        // 验证 dist 目录是否生成
        File distDir = new File(projectdir, "dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            log.error("构建完成但 dist 目录未生成：{}", projectPath);
            return false;
        }
        log.info("Vue 项目构建成功，dist 目录：{}", projectPath);
        return true;
    }

    /**
     * 执行 npm install 命令
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        // --ignore-scripts 是必须的：package.json 是 AI 生成的（也就是用户可影响的），
        // 而 npm install 默认会执行 preinstall / postinstall / prepare 这些生命周期脚本，
        // 等于给"在服务器上执行任意命令"开了个口子。
        String command = String.format("%s install --ignore-scripts", buildCommand("npm"));
        // 超时不能按"正常网速"给：本机实测冷装一次要 550 秒
        // （registry 返回单个包元数据最长 177 秒），300 秒会在装完之前就被强杀，
        // 构建永远不可能成功。这里给到 15 分钟。
        return executeCommand(projectDir, command, 900); // 15分钟超时
    }

    /**
     * 执行 npm run build 命令
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 180); // 3分钟超时
    }

    /**
     * 根据操作系统构造命令
     *
     * @param baseCommand
     * @return
     */
    private String buildCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }


    /**
     * 操作系统检测
     *
     * @return
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 执行命令
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否执行成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        // 子进程输出必须重定向到文件，不能沿用 Runtime.exec 默认的管道：
        // 父进程不读取子进程 stdout 时，管道缓冲区（Windows 约 4KB）写满后
        // npm 会永久阻塞在写操作上，waitFor 只能干等到超时再强杀进程。
        // 表现就是每次构建都白白耗满超时时间后失败，而且看不到 npm 的任何报错。
        File outputFile = new File(workingDir, BUILD_LOG_FILE_NAME);
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            ProcessBuilder processBuilder = new ProcessBuilder(command.split("\\s+"));
            processBuilder.directory(workingDir);
            // 合并 stderr，让 npm 的报错也落在同一个日志里
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(outputFile));
            Process process = processBuilder.start();
            // 等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程，输出见 {}", timeoutSeconds, outputFile.getAbsolutePath());
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}，输出见 {}", command, outputFile.getAbsolutePath());
                return true;
            } else {
                log.error("命令执行失败，退出码: {}，输出见 {}", exitCode, outputFile.getAbsolutePath());
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }
    }

}
