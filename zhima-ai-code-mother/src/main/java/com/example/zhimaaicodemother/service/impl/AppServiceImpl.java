package com.example.zhimaaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.zhimaaicodemother.Exception.BusinessException;
import com.example.zhimaaicodemother.Exception.ErrorCode;
import com.example.zhimaaicodemother.Exception.ThrowUtils;
import com.example.zhimaaicodemother.ai.AiCodeGenNameService;
import com.example.zhimaaicodemother.ai.AiCodeGenTypeRoutingService;
import com.example.zhimaaicodemother.ai.AiCodeGenTypeRoutingServiceFactory;
import com.example.zhimaaicodemother.constant.AppConstant;
import com.example.zhimaaicodemother.core.AiCodeGeneratorFacade;
import com.example.zhimaaicodemother.core.builder.VueProjectBuilder;
import com.example.zhimaaicodemother.core.handler.StreamHandlerExecutor;
import com.example.zhimaaicodemother.mapper.AppMapper;
import com.example.zhimaaicodemother.model.dto.app.AppAddRequest;
import com.example.zhimaaicodemother.model.dto.app.AppQueryRequest;
import com.example.zhimaaicodemother.model.entity.App;
import com.example.zhimaaicodemother.model.entity.User;
import com.example.zhimaaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.example.zhimaaicodemother.model.enums.CodeGenTypeEnum;
import com.example.zhimaaicodemother.model.vo.AppVO;
import com.example.zhimaaicodemother.model.vo.UserVO;
import com.example.zhimaaicodemother.monitor.MonitorContext;
import com.example.zhimaaicodemother.monitor.MonitorContextHolder;
import com.example.zhimaaicodemother.service.AppService;
import com.example.zhimaaicodemother.service.ChatHistoryService;
import com.example.zhimaaicodemother.service.ScreenshotService;
import com.example.zhimaaicodemother.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 应用 服务层实现类
 * @author fanren
 */
//将业务类注册到ioc容器，生成Bean
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper,App> implements AppService {
    //从配置中直接读取属性，没有则为默认值http://localhost
    @Value("${code.deploy-host:http://localhost}")
    private String deployHost;
    @Resource
    private UserService userService;
    @Resource//写在成员变量或set方法上，依赖注入，从ioc容器中获取Bean
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory; //注入ai智能路由服务工厂
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;
    @Resource
    private ScreenshotService screenshotService;
    @Resource
    private VueProjectBuilder vueProjectBuilder;
    @Resource
    private AiCodeGenNameService aiCodeGenNameService;

    /**
     * 通过对话生成应用代码
     *
     * @param appId     应用 ID
     * @param message   提示词
     * @param loginUser
     * @return
     */
    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 权限校验，仅本人可以和自己的应用对话
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用代码生成类型错误");
        }
        // 5. 校验并占用一次每日生成次数（管理员不限；普通用户超额会抛业务异常，
        //    由 GlobalExceptionHandler 转成 SSE 的 business-error 事件推给前端）
        userService.consumeDailyGenQuota(loginUser);
        // 6. 在调用 AI 前，先保存用户消息到数据库中
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // 7. 设置监控上下文（用户 ID 和应用 ID）
        MonitorContextHolder.setContext(
                MonitorContext.builder()
                        .userId(loginUser.getId().toString())
                        .appId(appId.toString())
                        .build()
        );
        // 8. 调用 AI 生成代码（流式）
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        // 9. 收集 AI 响应的内容，并且在完成后保存记录到对话历史
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum)
                .doFinally(signalType -> {
                    // 流结束时清理（无论成功/失败/取消）
                    MonitorContextHolder.clearContext();
                });
    }

    /**
     * 部署应用
     * @param appId     应用 ID
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId==null,ErrorCode.PARAMS_ERROR,"参数为空");
        ThrowUtils.throwIf(loginUser==null,ErrorCode.PARAMS_ERROR,"参数为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        // 3. 权限校验，仅本人可以部署自己的应用
        if(!loginUser.getId().equals(app.getUserId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限访问");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 如果没有，则生成 6 位 deployKey（字母 + 数字）
        if(StrUtil.isBlank(deployKey)){
            deployKey=RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，获取原始代码生成路径（应用访问目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType+"_"+appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR+File.separator+sourceDirName;
        // 6. 检查路径是否存在
        File sourceDir = new File(sourceDirPath);
        if(!sourceDir.exists()||!sourceDir.isDirectory()){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"应用路径不存在，请先生成应用");
        }
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请重试");
            // 检查 dist 目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 构建完成后，需要将构建后的文件复制到部署目录
            sourceDir = distDir;
        }
        // 8. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR+File.separator+deployKey;
        try{
            FileUtil.copyContent(sourceDir,new File(deployDirPath),true);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用部署失败："+e.getMessage());
        }
        // 9. 更新数据库
        App updateApp = new App();
        updateApp.setDeployKey(deployKey);
        updateApp.setId(appId);
        updateApp.setEditTime(LocalDateTime.now());
        boolean result = this.updateById(updateApp);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"更新应用部署信息失败");
        // 10. 构建应用访问 URL
        String appDeployUrl = String.format("%s/%s/",deployHost,deployKey);
        //异步生成截图并更新应用封面
        generateAppScreenshotAsync(appId,appDeployUrl);
        return appDeployUrl;
    }

    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId 应用id
     * @param appUrl 应用访问URL
     */
    @Override
    public void generateAppScreenshotAsync(Long appId,String appUrl){
        //使用虚拟线程异步执行
        Thread.startVirtualThread(()->{
            //调用截图服务
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            //更新应用封面字段
            App updateApp = new App();
            updateApp.setId((appId));
            updateApp.setCover(screenshotUrl);
            boolean updated=this.updateById(updateApp);
            ThrowUtils.throwIf(!updated,ErrorCode.OPERATION_ERROR,"更新应用封面字段失败");
        });
    }

    /**
     * 创建应用
     *
     * @param appAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser) {
        //参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化prompt不能为空");
        //构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        //应用名称ai生成
        app.setAppName(aiCodeGenNameService.generateName(initPrompt));
        //使用ai智能选择代码生成类型（多例模式）
        AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
        CodeGenTypeEnum selectedCodeGenTypeEnum = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(selectedCodeGenTypeEnum.getValue());
        //插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("应用创建成功,ID:{},类型：{}", app.getId(), selectedCodeGenTypeEnum.getValue());
        return app.getId();
    }

    /**
     * 获取应用封装类
     *
     * @param app
     * @return
     */
    @Override
    public AppVO getAppVO(App app){
        if(app == null){
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app,appVO);
        //关联查询用户信息
        Long userId = app.getUserId();
        if(userId!=null){
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }
    /**
     * 获取应用封装类列表
     *
     * @param appList
     * @return
     */
    @Override
    public List<AppVO> getAppVOList(List<App> appList){
        if(CollUtil.isEmpty(appList)){
            return new ArrayList<>();
        }
        Set<Long> userIdSet = new HashSet<>();//用Set去重
        for(App app : appList){
            userIdSet.add(app.getUserId());
        }
        //批量查询用户实体
        List<User> userList = userService.listByIds(userIdSet);
        //批量获取userVO
        Map<Long,UserVO> userVOMap = new HashMap<>();
        for( User user:userList){
            UserVO userVO = userService.getUserVO(user);
            userVOMap.put(user.getId(),userVO);
        }
        //将userVO转换为appVO
        List<AppVO> result = new ArrayList<>(appList.size());
        for(App app:appList){
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            result.add(appVO);
        }
        return result;
    }
    /**
     * 构造应用查询条件
     *
     * @param appQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();//排序字段
        String sortOrder = appQueryRequest.getSortOrder();//排序方式
        return QueryWrapper.create()
                .eq("id",id)
                .like("appName",appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField,"ascend".equals(sortField));
    }
}
