package com.example.zhimaaicodemother.ai.model.message;

import dev.langchain4j.service.tool.ToolExecution;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 工具执行结果消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ToolExecutedMessage extends StreamMessage {

    private String id;

    private String name;

    private String arguments;

    private String result;
    //ToolExecution toolExecution大模型调用工具的请求对象
    public ToolExecutedMessage(ToolExecution toolExecution){
       // 必须调用 super 设置消息类型：
       // 漏掉这一句时 type 字段为 null，序列化出去是 {"type":null,...}，
       // 下游 JsonMessageStreamHandler 用 getEnumByValue(null) 拿到 null，
       // switch(typeEnum) 会抛 NullPointerException: StreamMessageTypeEnum.ordinal() because "typeEnum" is null
       super(StreamMessageTypeEnum.TOOL_EXECUTED.getValue());
       this.id = toolExecution.request().id();
       this.name = toolExecution.request().name();
       this.arguments = toolExecution.request().arguments();
      this.result = toolExecution.result();
    }
}
