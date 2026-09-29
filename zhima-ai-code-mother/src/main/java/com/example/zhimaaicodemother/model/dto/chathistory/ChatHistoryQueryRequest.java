package com.example.zhimaaicodemother.model.dto.chathistory;

import com.example.zhimaaicodemother.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 查询历史对话请求
 */
@EqualsAndHashCode(callSuper = true)//生成的equal判断父类属性
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *id
     */
    private long id;
    /**
     * 消息
     */
    private String message;
    /**
     * user/ai
     */
    private String messageType;
    /**
     * appId
     */
    private Long appId;
    /**
     * userId
     */
    private Long userId;
    /**
     * 最后一条记录的时间
     */
    private LocalDateTime lastCreateTime;

}
