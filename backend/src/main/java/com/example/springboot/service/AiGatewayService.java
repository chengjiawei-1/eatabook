package com.example.springboot.service;

import com.example.springboot.ai.AiPromptTemplateService;
import com.example.springboot.ai.AiScene;
import com.example.springboot.config.AiGatewayProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AiGatewayService {

    private final ChatClient chatClient;
    private final ImageModel imageModel;
    private final AiGatewayProperties properties;
    private final AiPromptTemplateService promptTemplateService;

    public AiGatewayService(ChatClient.Builder chatClientBuilder,
                            ObjectProvider<ImageModel> imageModelProvider,
                            AiGatewayProperties properties,
                            AiPromptTemplateService promptTemplateService) {
        this.chatClient = chatClientBuilder.build();
        this.imageModel = imageModelProvider.getIfAvailable();
        this.properties = properties;
        this.promptTemplateService = promptTemplateService;
    }

    public String chat(AiScene scene, String prompt) {
        return chat(scene, prompt, null);
    }

    public String chat(AiScene scene, String prompt, Map<String, Object> meta) {
        try {
            String content = withSceneOptions(chatClient.prompt(), scene)
                    .user(buildPrompt(scene, prompt, meta))
                    .call()
                    .content();
            return sanitizeOutput(scene, content);
        } catch (RuntimeException e) {
            throw toResponseStatusException(e);
        }
    }

    public Flux<String> stream(AiScene scene, String prompt) {
        return stream(scene, prompt, null);
    }

    public Flux<String> stream(AiScene scene, String prompt, Map<String, Object> meta) {
        return withSceneOptions(chatClient.prompt(), scene)
                .user(buildPrompt(scene, prompt, meta))
                .stream()
                .content()
                .map(content -> sanitizeOutput(scene, content))
                .onErrorResume(error -> Flux.just(toStreamErrorMessage(error)))
                .concatWithValues("[DONE]");
    }

    public List<String> generateImages(AiScene scene, String prompt, Map<String, Object> meta) {
        if (imageModel == null) {
            throw new IllegalStateException("Spring AI ImageModel is not configured");
        }

        OpenAiImageOptions options = OpenAiImageOptions.builder()
                .N(properties.getImage().getCount())
                .responseFormat(properties.getImage().getResponseFormat())
                .build();
        options.setSize(properties.getImage().getSize());

        ImageResponse response = imageModel.call(new ImagePrompt(buildPrompt(scene, prompt, meta), options));
        return response.getResults().stream()
                .map(ImageGeneration::getOutput)
                .filter(Objects::nonNull)
                .map(this::toImageValue)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toList());
    }

    public byte[] synthesizeSpeech(String text, Map<String, Object> meta) {
        if (!properties.getAudio().isEnabled()) {
            throw new UnsupportedOperationException("AI speech is not enabled");
        }
        throw new UnsupportedOperationException("AI speech gateway is not implemented yet");
    }

    private String buildPrompt(AiScene scene, String prompt, Map<String, Object> meta) {
        return promptTemplateService.render(scene, prompt, meta);
    }

    private String sanitizeOutput(AiScene scene, String content) {
        if (content == null || scene != AiScene.ANCIENT_TEXT_TRANSLATION) {
            return content;
        }
        return content.replace("*", "");
    }

    private ChatClient.ChatClientRequestSpec withSceneOptions(ChatClient.ChatClientRequestSpec requestSpec,
                                                              AiScene scene) {
        OpenAiChatOptions options = buildChatOptions(scene);
        return options == null ? requestSpec : requestSpec.options(options);
    }

    private OpenAiChatOptions buildChatOptions(AiScene scene) {
        AiGatewayProperties.SceneProperties sceneProperties = properties.getScenes().get(scene.getTemplateKey());
        if (sceneProperties == null) {
            return null;
        }

        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder();
        boolean configured = false;

        if (hasText(sceneProperties.getModel())) {
            builder.model(sceneProperties.getModel().trim());
            configured = true;
        }
        if (sceneProperties.getTemperature() != null) {
            builder.temperature(sceneProperties.getTemperature());
            configured = true;
        }
        if (sceneProperties.getTopP() != null) {
            builder.topP(sceneProperties.getTopP());
            configured = true;
        }
        if (sceneProperties.getMaxTokens() != null) {
            builder.maxTokens(sceneProperties.getMaxTokens());
            configured = true;
        }

        if (!configured) {
            return null;
        }
        builder.streamUsage(false);
        return builder.build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private ResponseStatusException toResponseStatusException(Throwable error) {
        WebClientResponseException webClientError = findWebClientResponseException(error);
        if (webClientError == null) {
            return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI service request failed", error);
        }
        if (webClientError.getStatusCode().value() == 429) {
            return new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    appendUpstreamDetail("AI 服务请求过于频繁，请稍后重试或检查模型额度", webClientError),
                    webClientError
            );
        }
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                appendUpstreamDetail("AI 服务调用失败：" + webClientError.getStatusCode(), webClientError),
                webClientError
        );
    }

    private String toStreamErrorMessage(Throwable error) {
        WebClientResponseException webClientError = findWebClientResponseException(error);
        if (webClientError != null && webClientError.getStatusCode().value() == 429) {
            return appendUpstreamDetail("AI 服务请求过于频繁，请稍后重试或检查硅基流动模型额度。", webClientError);
        }
        if (webClientError != null) {
            return appendUpstreamDetail("AI 服务调用失败：" + webClientError.getStatusCode() + "。", webClientError);
        }
        return "AI 服务调用失败，请稍后重试。";
    }

    private String appendUpstreamDetail(String message, WebClientResponseException webClientError) {
        String detail = normalizeUpstreamDetail(webClientError.getResponseBodyAsString());
        return detail.isEmpty() ? message : message + " 上游信息：" + detail;
    }

    private String normalizeUpstreamDetail(String body) {
        if (!hasText(body)) {
            return "";
        }
        String detail = body.replaceAll("\\s+", " ").trim();
        return detail.length() <= 200 ? detail : detail.substring(0, 200) + "...";
    }

    private WebClientResponseException findWebClientResponseException(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof WebClientResponseException webClientResponseException) {
                return webClientResponseException;
            }
            current = current.getCause();
        }
        return null;
    }

    private String toImageValue(Image image) {
        if (image.getUrl() != null && !image.getUrl().isBlank()) {
            return image.getUrl();
        }
        if (image.getB64Json() != null && !image.getB64Json().isBlank()) {
            return "data:image/png;base64," + image.getB64Json();
        }
        return null;
    }
}
