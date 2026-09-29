package com.example.zhimaaicodemother.controller;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.example.zhimaaicodemother.Exception.BusinessException;
import com.example.zhimaaicodemother.Exception.ErrorCode;
import com.example.zhimaaicodemother.Exception.ThrowUtils;
import com.example.zhimaaicodemother.annotion.AuthCheck;
import com.example.zhimaaicodemother.common.BaseResponse;
import com.example.zhimaaicodemother.common.DeleteRequest;
import com.example.zhimaaicodemother.common.ResultUtils;
import com.example.zhimaaicodemother.constant.UserConstant;
import com.example.zhimaaicodemother.manager.CosManager;
import com.example.zhimaaicodemother.model.dto.user.*;
import com.example.zhimaaicodemother.model.entity.User;
import com.example.zhimaaicodemother.model.vo.LoginUserVO;
import com.example.zhimaaicodemother.model.vo.UserVO;
import com.example.zhimaaicodemother.ratelimter.annotation.RateLimit;
import com.example.zhimaaicodemother.ratelimter.enums.RateLimitType;
import com.example.zhimaaicodemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 用户 控制层。
 *
 * @author fanren
 */
@Slf4j
@RestController//接收前端http请求，返回json
@RequestMapping("/user")//接口地址拼接user
public class UserController {
    @Resource//resource注入userService
    private UserService userService;

    @Resource
    private CosManager cosManager;
    /**
     * 用户注册
     * @param userRegisterRequest
     * @return 注册结果
     */
    @PostMapping("/register")
    @RateLimit(limitType = RateLimitType.IP, rate = 1, rateInterval = 259200, message = "注册过于频繁，请稍后再试")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        Long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request          请求对象
     * @return 脱敏后的用户登录信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }
    /**
     * 用户注销
     * @param request
    * @return 注销结果
     */
    @PostMapping("/logout")
    BaseResponse<Boolean> userLogout(HttpServletRequest request){
        ThrowUtils.throwIf(request == null,ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }
    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest){
        ThrowUtils.throwIf(userAddRequest == null,ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userAddRequest,user);
        //默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);//向数据库插入一条记录
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }
    /**
     * 根据id获取用户(仅管理员)
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id){
        ThrowUtils.throwIf(id<0,ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null,ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }
    /**
     * 根据id获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }
    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest){
        if(deleteRequest == null || deleteRequest.getId() < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }
    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
        if(userUpdateRequest == null || userUpdateRequest.getId() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        user.setId(userUpdateRequest.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    /**
     * 用户自己更新自己信息
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyInfo(@RequestBody UserUpdateMyRequest userUpdateMyRequest,HttpServletRequest request){
        ThrowUtils.throwIf(userUpdateMyRequest == null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserName(userUpdateMyRequest.getUserName());
        user.setUserAvatar(userUpdateMyRequest.getUserAvatar());
        user.setUserProfile(userUpdateMyRequest.getUserProfile());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 上传头像到对象存储，返回可访问的图片地址。
     *
     * 只负责「存文件 + 返回 URL」，不直接改数据库：
     * 前端拿到 URL 后填进个人资料表单，用户点「保存修改」再调 /user/update/my 落库。
     * 这样用户可以先预览、不满意就换，也避免"传了就等于改了"。
     *
     * 注意路径写的是 "/avatar/upload" 而不是 "/user/avatar/upload"：
     * 类上已经有 @RequestMapping("/user")，再写全路径会拼成 /user/user/avatar/upload。
     *
     * @param multipartFile 前端 multipart 报文里 name 为 file 的那一段
     * @return 图片在 COS 上的访问地址
     */
    @PostMapping("/avatar/upload")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile multipartFile,
                                             HttpServletRequest request) {
        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(),
                ErrorCode.PARAMS_ERROR, "文件为空");
        // 未登录会抛 40100
        User loginUser = userService.getLoginUser(request);

        // 大小兜底：multipart 的配置是第一道，这里是为了能返回友好的业务提示
        ThrowUtils.throwIf(multipartFile.getSize() > 5 * 1024 * 1024L,
                ErrorCode.PARAMS_ERROR, "头像不能超过 5MB");

        // 类型白名单：文件名和 Content-Type 都是客户端说了算的，不能直接相信
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!Set.of("jpg", "jpeg", "png", "webp", "gif")
                        .contains(StrUtil.blankToDefault(suffix, "").toLowerCase()),
                ErrorCode.PARAMS_ERROR, "只支持 jpg / png / webp / gif");

        // 对象键完全由服务端生成：直接拼客户端文件名会有路径穿越、覆盖他人文件的风险；
        // 带 UUID 是为了防止重名覆盖，同时让 URL 变化、避免浏览器一直用缓存的旧头像。
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = loginUser.getId() + "_" + UUID.randomUUID().toString().substring(0, 8)
                + "." + suffix.toLowerCase();
        String cosKey = String.format("/user/avatar/%s/%s", datePath, fileName);

        File tempFile = null;
        try {
            // CosManager 只接受 File，所以先把上传流转存成本地临时文件
            tempFile = File.createTempFile("avatar-", "." + suffix);
            multipartFile.transferTo(tempFile);
            String url = cosManager.uploadFile(cosKey, tempFile);
            ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.OPERATION_ERROR, "头像上传失败");
            return ResultUtils.success(url);
        } catch (IOException e) {
            log.error("头像上传失败，userId={}", loginUser.getId(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败");
        } finally {
            // 无论成功失败都要删，否则每传一次就在系统临时目录留一个文件
            if (tempFile != null) {
                FileUtil.del(tempFile);
            }
        }
    }
    /**
     * 分页获取用户封装列表(仅管理员)
     * @param userQueryRequest
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOListByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize), userService.getQueryWrapper(userQueryRequest));
        //数据脱敏
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
}
