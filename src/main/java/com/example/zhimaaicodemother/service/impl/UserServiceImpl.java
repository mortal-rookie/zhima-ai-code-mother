package com.example.zhimaaicodemother.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.example.zhimaaicodemother.constant.UserConstant;
import com.example.zhimaaicodemother.Exception.BusinessException;
import com.example.zhimaaicodemother.Exception.ErrorCode;
import com.example.zhimaaicodemother.mapper.UserMapper;
import com.example.zhimaaicodemother.model.dto.user.UserQueryRequest;
import com.example.zhimaaicodemother.model.entity.User;
import com.example.zhimaaicodemother.model.enums.UserRoleEnum;
import com.example.zhimaaicodemother.model.vo.LoginUserVO;
import com.example.zhimaaicodemother.model.vo.UserVO;
import com.example.zhimaaicodemother.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.example.zhimaaicodemother.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现类
 * @author fanren
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{
    /**
     * 注册新用户
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param chekPassword 验证密码
     * @return 新用户id
     */
    @Override
    public Long userRegister(String userAccount, String userPassword, String chekPassword){
        //1.校验参数
        //StrUtil.hasBlank()hutool工具类函数，批量判断字符串是否为空
        if(StrUtil.hasBlank(userAccount,userPassword,chekPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度过短");
        }
        if(userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }
        if(!userPassword.equals(chekPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }
        //2.查询用户是否已经存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount",userAccount);
        Long count = this.mapper.selectCountByQuery(queryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已经存在");
        }
        //3.加密密码
        String encryptPassword = getEncryptPassword(userPassword);
        //创建用户，插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("默认用户");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户注册失败,数据库错误");
        }
        return user.getId();
    }
    /**
     * 获取脱敏的已登录的用户信息
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user){
        if(user == null){
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        //BeanUtil.copyProperties() hutool工具类函数，复制对象属性，目标没有的属性跳过
        BeanUtil.copyProperties(user,loginUserVO);
        return loginUserVO;
    }
    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request){
        //1.校验参数
        if(StrUtil.hasBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度过短");
        }
        if(userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }
        //2.加密
        String encryptPassword = getEncryptPassword(userPassword);
        //3.查询用户是否存在
        QueryWrapper queryWarapper = new QueryWrapper();
        queryWarapper.eq("userAccount",userAccount);
        queryWarapper.eq("userPassword",userPassword);
        User user = this.mapper.selectOneByQuery(queryWarapper);
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }
        //4.用户已存在，记录用户的登陆状态
        request.getSession().setAttribute(USER_LOGIN_STATE,user);
        //5.返回脱敏后的用户信息
        return getLoginUserVO(user);

    }
    /**
     * 获取当前登录用户
     * @param request
     * @return User
     */
    @Override
    public User getLoginUser(HttpServletRequest request){
        //先判断用户是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User)userObj;
        if(currentUser==null || currentUser.getId()==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //从数据库查询当前用户信息
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }
    /**
     * 获取脱敏后的用户信息
     * @param user 用户
     * @return UserVO
     */
    @Override
    public UserVO getUserVO(User user){
        if(user == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user,userVO);
        return userVO;
    }
    /**
     * 获取脱敏后的用户信息（分页）
     * @param userList 用户列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList){
        if(CollUtil.isEmpty(userList)){
            return new ArrayList<>();
        }
        List<UserVO> voList = new ArrayList<>();
        for(User user:userList){
            UserVO userVO = this.getUserVO(user);
            voList.add(userVO);
        }
        return voList;
    }
    /**
     * 用户注销
     * @param request
     * @return 退出登录是否成功
     */
    @Override
    public Boolean userLogout(HttpServletRequest request){
//        先判断用户是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"用户未登录");
        }
//        移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }
    /**
     * 根据条件构造数据查询参数
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest){
        if(userQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();//排序字段
        String sortOrder = userQueryRequest.getSortOrder();//排列方式
        return QueryWrapper.create()
                .eq("id",id)
                .eq("userRole",userRole)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }
    /**
     * 加密密码
     */
    @Override
    public String getEncryptPassword(String userPassword){
        //盐值，混淆密码
        final String SALT = "fanren";
        return DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes(StandardCharsets.UTF_8));
    }
}
