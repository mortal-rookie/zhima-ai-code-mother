package com.example.zhimaaicodemother.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.zhimaaicodemother.Exception.BusinessException;
import com.example.zhimaaicodemother.Exception.ErrorCode;
import com.example.zhimaaicodemother.ai.AiCodeGeneratorService;
import com.example.zhimaaicodemother.ai.AiCodeGeneratorServiceFactory;
import com.example.zhimaaicodemother.ai.model.HtmlCodeResult;
import com.example.zhimaaicodemother.ai.model.MultiFileCodeResult;
import com.example.zhimaaicodemother.ai.model.message.AiResponseMessage;
import com.example.zhimaaicodemother.ai.model.message.ToolExecutedMessage;
import com.example.zhimaaicodemother.ai.model.message.ToolRequestMessage;
import com.example.zhimaaicodemother.constant.AppConstant;
import com.example.zhimaaicodemother.model.enums.CodeGenTypeEnum;
import com.example.zhimaaicodemother.core.saver.CodeFileSaverExecutor;
import com.example.zhimaaicodemother.core.parser.CodePaserExecutor;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import com.example.zhimaaicodemother.core.builder.VueProjectBuilder;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.io.File;

/**
 * Ai 代码生成门面类，组合代码生成和保存功能
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId           应用 ID
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据 appId 获取相应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId           应用 ID
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据 appId 获取相应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream, appId, CodeGenTypeEnum.VUE_PROJECT);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @param appId       应用 ID
     * @param codeGenType 生成类型（失败时用来定位并清掉缓存的 AI 服务实例）
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId, CodeGenTypeEnum codeGenType) {
        return Flux.create(sink -> {
                    // 客户端断开时 Reactor 会回调这里。上游 TokenStream 没有取消接口，
                    // 无法真正止住模型推理，至少让"被放弃的生成"在日志里可见。
                    sink.onCancel(() -> log.warn("客户端已断开，appId={} 的流式响应被放弃", appId));

                    tokenStream.onPartialResponse((String partialResponse) -> {
                                AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                                // sink 会被 HTTP IO 线程与工具执行线程并发调用，
                                // Reactor 要求信号串行，这里加锁保证不会交错
                                synchronized (sink) {
                                    sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                                }
                            })
                            .onPartialToolCall((PartialToolCall partialToolCall) -> {
                                log.debug("工具调用参数分片，appId={}，index={}，name={}",
                                        appId, partialToolCall.index(), partialToolCall.name());
                            })
                            .onToolExecuted((ToolExecution toolExecution) -> {
                                // request() 给出的是完整参数，比 onPartialToolCall 的分片更可靠
                                ToolExecutionRequest request = toolExecution.request();
                                // 每次工具执行都记一行。
                                // 之前除 writeFile/exit 外所有工具都没有任何日志，
                                // 结果"模型烧光 50 轮却一个文件都没写"这种情况完全无法取证：
                                // 分不清它是在反复 readDir、还是 modifyFile 一直失败、
                                // 还是在调不存在的工具。参数可能很长（writeFile 带整个文件内容），
                                // 必须截断，否则日志会被文件内容淹没。
                                log.info("工具执行 appId={} | 工具={} | 失败={} | 耗时={}ms | 参数={} | 结果={}",
                                        appId,
                                        request.name(),
                                        toolExecution.hasFailed(),
                                        toolExecution.duration() == null ? -1 : toolExecution.duration().toMillis(),
                                        StrUtil.maxLength(request.arguments(), 120),
                                        StrUtil.maxLength(toolExecution.result(), 120));
                                ToolRequestMessage toolRequestMessage = new ToolRequestMessage(request);
                                synchronized (sink) {
                                    sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                                }

                                ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                                synchronized (sink) {
                                    sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                                }
                            })
                            .onCompleteResponse((ChatResponse response) -> {
                                // 先收流，再异步构建，顺序不能反：
                                // 首次 npm install 在本机实测要 ~9 分钟（拉 registry 元数据极慢，
                                // 单个包 177 秒），如果等构建完再 complete，前端会一直停在
                                // "正在生成网站…"，用户根本分不清是在跑还是卡死了。
                                // 代价是 done 到达时 dist 通常还没生成，
                                // 所以前端额外提供了"刷新预览"按钮来补这一步。
                                synchronized (sink) {
                                    sink.complete();
                                }

                                // 构建放到弹性线程池：既不能占用模型回调线程，
                                // 也不能拖住已经收尾的 SSE 流。
                                Schedulers.boundedElastic().schedule(() -> {
                                    try {
                                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR
                                                + File.separator + "vue_project_" + appId;
                                        boolean buildSuccess = vueProjectBuilder.buildProject(projectPath);

                                        if (buildSuccess) {
                                            log.info("Vue 项目构建成功，appId={}，路径={}", appId, projectPath);
                                        } else {
                                            log.error("Vue 项目构建失败，appId={}，路径={}", appId, projectPath);
                                        }
                                    } catch (Exception e) {
                                        log.error("Vue 项目构建异常，appId=" + appId, e);
                                    }
                                });
                            })
                            .onError((Throwable error) -> {
                                log.error("流式生成失败，appId=" + appId, error);
                                // 失败后必须让这个应用的 AI 服务实例失效。
                                // 记忆里可能残留一条 assistant(tool_calls) 后面缺少 tool 响应的
                                // 孤儿消息（工具轮次超限时 langchain4j 是先把它写进记忆再抛异常），
                                // 不清掉的话，该应用之后每一次请求都会被 DeepSeek 直接 400 拒绝：
                                //   An assistant message with 'tool_calls' must be followed by
                                //   tool messages responding to each 'tool_call_id'.
                                // 而缓存的实例会一直复用，只能靠重启后端才能恢复。
                                // 让实例失效后，下一次会重建记忆（重建时先 clear），自动自愈。
                                aiCodeGeneratorServiceFactory.invalidateAiCodeGeneratorService(appId, codeGenType);
                                synchronized (sink) {
                                    sink.error(error);
                                }
                            })
                            .start();
                },
                // 上游是异步回调推送，突发时先缓冲，避免下游来不及消费时丢信号
                FluxSink.OverflowStrategy.BUFFER);
    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @param appId       应用 ID
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        // 字符串拼接器，用于当流式返回所有的代码之后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            // 实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式返回完成后，保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodePaserExecutor.executeParser(completeCode, codeGenType);
                // 使用执行器保存代码
                File saveDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                log.info("保存成功，目录为：{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }
}
