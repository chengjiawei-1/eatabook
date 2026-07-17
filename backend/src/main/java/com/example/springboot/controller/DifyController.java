package com.example.springboot.controller;

import com.example.springboot.config.DifyConfig;
import com.example.springboot.helper.DifyHelper;
import io.github.imfangs.dify.client.model.chat.ChatMessage;
import io.github.imfangs.dify.client.enums.ResponseMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Tag(name="DifyController",description="Dify相关接口")
@CrossOrigin
@RestController
@RequestMapping("/api")
public class DifyController {

    @Autowired
    private DifyHelper difyHelper;

    @Autowired
    private DifyConfig difyConfig;

// 结构化接口
    @Operation(summary = "结构化智能体", description = "接收 POST 请求并返回 JSON 结果")
    @PostMapping("/ai/chat/structured")
    public String chatStructured(@RequestBody ChatPayload payload) {
        //获取配置
        DifyConfig.AgentInfo agentInfo = difyConfig.getAgents().get(payload.getAgent());
        if (agentInfo == null) return "{\"error\":\"未找到配置\"}";
        //构造消息
        ChatMessage msg = ChatMessage.builder()
                .query(agentInfo.getPrefix() + payload.getPrompt())
                .user("user-unique-id")
                .inputs(new java.util.HashMap<>())
                .responseMode(ResponseMode.BLOCKING)
                .build();
        String jsonString = difyHelper.chatBlocking(agentInfo.getApiKey(), msg);
        //调用 Dify 并返回结果
        return jsonString;
    }

    //POST流式接口
    @Operation(summary = "流式智能体", description = "智能体流式回复")
    @PostMapping(value = "/ai/chat/stream", produces = "text/event-stream")
    public Flux<String> chatStream(@RequestBody ChatPayload payload) {
        DifyConfig.AgentInfo agentInfo = difyConfig.getAgents().get(payload.getAgent());
        if (agentInfo == null) {
            return Flux.just("未找到该智能体配置：" + payload.getAgent());
        }

        ChatMessage msg = ChatMessage.builder()
                .query(agentInfo.getPrefix() + payload.getPrompt())
                .user("user-unique-id")
                .inputs(new java.util.HashMap<>())
                .responseMode(ResponseMode.STREAMING)
                .build();

        return difyHelper.chatStream(agentInfo.getApiKey(), msg);
    }
}

