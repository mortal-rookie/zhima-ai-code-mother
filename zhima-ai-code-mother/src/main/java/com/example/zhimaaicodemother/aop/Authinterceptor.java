package com.example.zhimaaicodemother.aop;

import com.example.zhimaaicodemother.Exception.BusinessException;
import com.example.zhimaaicodemother.Exception.ErrorCode;
import com.example.zhimaaicodemother.annotion.AuthCheck;
import com.example.zhimaaicodemother.model.entity.User;
import com.example.zhimaaicodemother.model.enums.UserRoleEnum;
import com.example.zhimaaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect//标注这是切面类
@Component
public class Authinterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     * @return 返回结果
    * @throws Throwable 异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable{
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取当前登录的对象
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        //不需要权限，直接放行
        if(mustRoleEnum == null){
            return joinPoint.proceed();
        }
        //以下代码，必须有这个才能通过
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        //没有权限，直接拒绝
        if(userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //要求必须要有管理员权限，但当前用户没有
        if(UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //通过普通用户的权限校验，放行
        return joinPoint.proceed();
    }
}
