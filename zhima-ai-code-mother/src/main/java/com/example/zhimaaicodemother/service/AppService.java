package com.example.zhimaaicodemother.service;

import com.example.zhimaaicodemother.model.dto.app.AppAddRequest;
import com.example.zhimaaicodemother.model.dto.app.AppQueryRequest;
import com.example.zhimaaicodemother.model.entity.App;
import com.example.zhimaaicodemother.model.entity.User;
import com.example.zhimaaicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层
 * @author fanren
 */
public interface AppService extends IService<App> {

    /**
     * 通过对话生成应用代码
     *
     * @param appId     应用 ID
     * @param message   提示词
     * @param loginUser 登录用户
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 创建应用
     *
     * @param appAddRequest
     * @param loginUser
     * @return
     */
    Long createApp(AppAddRequest appAddRequest, User loginUser);
    /**
     * 应用部署
     *
     * @param appId     应用 ID
     * @param loginUser 登录用户
     * @return 可访问的部署地址
     */
    String deployApp(Long appId, User loginUser);
    /**
     * 获取应用封装类
     * @param app
     * @return
     */
    AppVO getAppVO(App app);
    /**
     * 获取应用封装类列表
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);
    /**
     * 构造应用查询条件
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);
    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId 应用id
     * @param appUrl 应用访问URL
     */
    void generateAppScreenshotAsync(Long appId,String appUrl);
}
