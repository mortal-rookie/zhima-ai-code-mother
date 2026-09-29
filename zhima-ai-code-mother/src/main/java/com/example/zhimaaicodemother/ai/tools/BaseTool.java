package com.example.zhimaaicodemother.ai.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.example.zhimaaicodemother.constant.AppConstant;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 工具基类
 * 所有ai工具类的通用接口
 */
public abstract class BaseTool {

    /**获取工具的英文名称
     * @return 英文工具名
     */
    public abstract String getToolName();

    /**
     * 获取工具的中文名称
     * @return 中文工具名
     */
    public abstract String getDisplayName();

    /**
     * 生成工具请求时的返回值（显示给用户）
     *
     * @return 工具请求显示内容
     */
    public String generateToolRequestResponse(){
        return String.format("\n\n[使用工具]%s\n\n",getDisplayName());
    }

    /**
     * 生成工具执行结果格式（保存到数据库）
     *
     * @param arguments 工具执行参数
     * @return 格式化的工具执行结果
     */
    public abstract String generateToolExecutedResult(JSONObject arguments);

    /**
     * 取该应用的项目根目录。
     * 目录名必须和 CodeFileSaver 落盘时用的一致，否则工具会找不到文件。
     */
    protected Path getProjectRoot(Long appId) {
        return Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, "vue_project_" + appId)
                .toAbsolutePath()
                .normalize();
    }

    /**
     * 把 AI 给的路径安全地解析到「本应用自己的项目目录」内，越界一律返回 null。
     * <p>
     * 为什么每个工具都必须走这里：这些参数是模型生成的，而模型又受用户提示词影响，
     * 等于用户可以直接控制这个字符串。原来的写法是
     * <pre>
     *     Path path = Paths.get(relativeFilePath);
     *     if (!path.isAbsolute()) { path = projectRoot.resolve(relativeFilePath); }
     * </pre>
     * 有两个逃逸口：
     * <ol>
     *   <li>传绝对路径 —— 整个 if 被跳过，直接写到 {@code C:\...} 任意位置；</li>
     *   <li>传 {@code ../../..} —— {@code resolve} 不做规范化，照样跳出项目目录。</li>
     * </ol>
     * 两者都能演变成「远程任意文件写入 → 远程代码执行」。
     * 这里统一做两件事：拒绝绝对路径 + 规范化后校验仍在项目根目录内。
     *
     * @param appId        应用 id（决定项目目录）
     * @param relativePath 模型给出的相对路径
     * @return 项目目录内的绝对路径；非法路径返回 null，调用方必须拒绝执行
     */
    protected Path resolveSafePath(Long appId, String relativePath) {
        if (appId == null || StrUtil.isBlank(relativePath)) {
            return null;
        }
        Path raw = Paths.get(relativePath);
        // 绝对路径一律拒绝：绝对路径没有任何"应该在本项目目录内"的理由
        if (raw.isAbsolute()) {
            return null;
        }
        Path root = getProjectRoot(appId);
        // normalize() 会把 a/../../b 里的 .. 真正解开，再用 startsWith 判断有没有跑出去
        Path target = root.resolve(raw).normalize();
        if (!target.startsWith(root)) {
            return null;
        }
        return target;
    }

    /**
     * 越界时的统一提示语，交给模型让它换个路径重试，而不是让它以为"写成功了"
     */
    protected String illegalPathMessage(String originalPath) {
        return "错误：非法路径。只允许操作本应用项目目录内的相对路径，禁止绝对路径与 .. 跳转 - " + originalPath;
    }
}
