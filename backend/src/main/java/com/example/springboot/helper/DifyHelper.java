package com.example.springboot.helper;

import com.example.springboot.config.DifyConfig;
import io.github.imfangs.dify.client.DifyClient;
import io.github.imfangs.dify.client.DifyClientFactory;
import io.github.imfangs.dify.client.callback.ChatStreamCallback;
import io.github.imfangs.dify.client.enums.ResponseMode;
import io.github.imfangs.dify.client.event.MessageEvent;
import io.github.imfangs.dify.client.model.chat.ChatMessage;
import io.github.imfangs.dify.client.model.chat.MessageListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Component
public class DifyHelper {

    @Autowired
    private DifyConfig difyConfig;
    public Flux<String> chatStream(String apiKey, ChatMessage message) {
        //创建客户端
        DifyClient client = DifyClientFactory.createClient(difyConfig.getBaseUrl(), apiKey);
        //创建Flux流
        return Flux.create(emitter -> {
            try {
                client.sendChatMessageStream(message, new ChatStreamCallback() {
                    @Override
                    public void onMessage(MessageEvent event) {
                        emitter.next(event.getAnswer());
                    }

                    @Override
                    public void onMessageEnd(io.github.imfangs.dify.client.event.MessageEndEvent event) {
                        emitter.complete();
                    }

                    @Override
                    public void onError(io.github.imfangs.dify.client.event.ErrorEvent event) {
                        emitter.error(new RuntimeException(event.getMessage()));
                    }
                });
            } catch (Exception e) {
                emitter.error(e);
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    //创建blocking流
    public String chatBlocking(String apiKey, ChatMessage message) {
        message.setResponseMode(ResponseMode.BLOCKING);
        DifyClient client = DifyClientFactory.createClient(difyConfig.getBaseUrl(), apiKey);

        try {
            return client.sendChatMessage(message).getAnswer();
        } catch (Exception e) {
            return "请求出错：" + e.getMessage();
        }
    }
}