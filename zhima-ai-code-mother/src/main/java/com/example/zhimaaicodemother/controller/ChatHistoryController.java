package com.example.zhimaaicodemother.controller;

import com.example.zhimaaicodemother.Exception.ErrorCode;
import com.example.zhimaaicodemother.Exception.ThrowUtils;
import com.example.zhimaaicodemother.annotion.AuthCheck;
import com.example.zhimaaicodemother.common.BaseResponse;
import com.example.zhimaaicodemother.common.ResultUtils;
import com.example.zhimaaicodemother.constant.UserConstant;
import com.example.zhimaaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.example.zhimaaicodemother.model.entity.ChatHistory;
import com.example.zhimaaicodemother.model.entity.User;
import com.example.zhimaaicodemother.service.ChatHistoryService;
import com.example.zhimaaicodemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 历史对话 控制层
 * @author fanren
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    //@PathVariable从访问url中提取参数，@RequestParam(required = false)前端可以不传参，不传时默认为null
    // 必须以 / 开头：类上是 @RequestMapping("/chatHistory")，这里写成 "app/{appId}"
    // 会被 Spring 拼成 /chatHistoryapp/{appId}，而前端请求的是 /chatHistory/app/{appId}，
    // 结果对话历史接口永远 404，聊天页一条历史都加载不出来。
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId,pageSize,lastCreateTime,loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }

}
