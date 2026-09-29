package com.example.zhimaaicodemother.service;

import com.example.zhimaaicodemother.model.dto.user.UserQueryRequest;
import com.example.zhimaaicodemother.model.entity.User;
import com.example.zhimaaicodemother.model.vo.LoginUserVO;
import com.example.zhimaaicodemother.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 服务层接口
 * @author fanren
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param chekPassword 验证密码
     * @return 新用户id
     */
    Long userRegister(String userAccount, String userPassword, String chekPassword);//接口默认Public abstract 须在实现类重写接口方法
    /**
     * 获取脱敏的已登录的用户信息
     * @return 脱敏后的用户信息
     */
    LoginUserVO getLoginUserVO(User user);
    /**
     * 用户登录
    * @param userAccount 用户账号
    * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);
    /**
     * 获取当前登录用户
     * @param request
     * @return User
     */
    User getLoginUser(HttpServletRequest request);
    /**
     * 获取当前登录用户，未登录时返回 null（不抛异常）。
     * 用于「对外公开、但登录后能看到更多」的接口，例如查看已部署作品的详情。
     * @param request
     * @return 登录用户；未登录返回 null
     */
    User getLoginUserPermitNull(HttpServletRequest request);
    /**
     * 获取脱敏后的用户信息
     * @param user 用户
     * @return UserVO
     */
    UserVO getUserVO(User user);
    /**
     * 获取脱敏后的用户信息（分页）
     * @param userList 用户列表
     */
    List<UserVO> getUserVOList(List<User> userList);
    /**
     * 用户注销
     * @param request
     * @return 退出登录是否成功
     */
    Boolean userLogout(HttpServletRequest request);
    /**
     * 根据条件构造数据查询参数
     * @param userQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);
    /**
     * 加密
     * @param userPassword
     * @return 加密后的用户密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 校验并占用一次「每日免费生成次数」。
     * 管理员不限次数，直接放行；普通用户超额时抛业务异常。
     * @param loginUser 当前登录用户
     */
    void consumeDailyGenQuota(User loginUser);

}