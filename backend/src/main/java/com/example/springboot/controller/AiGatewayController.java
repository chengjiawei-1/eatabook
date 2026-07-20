package com.example.springboot.controller;

import com.example.springboot.ai.AiScene;
import com.example.springboot.config.AiGatewayProperties;
import com.example.springboot.service.AiGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "AiGatewayController", description = "Spring AI gateway APIs")
@CrossOrigin
@RestController
@RequestMapping("/api")
public class AiGatewayController {

    private final AiGatewayService aiGatewayService;
    private final AiGatewayProperties aiGatewayProperties;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:}")
    private String chatModel;

    public AiGatewayController(AiGatewayService aiGatewayService,
                               AiGatewayProperties aiGatewayProperties) {
        this.aiGatewayService = aiGatewayService;
        this.aiGatewayProperties = aiGatewayProperties;
    }

    @Operation(summary = "Structured AI chat", description = "Receive a POST request and return a blocking result")
    @PostMapping({"/ai/chat", "/ai/chat/structured"})
    public String chatStructured(@RequestBody ChatPayload payload) {
        return aiGatewayService.chat(resolveScene(payload), resolvePrompt(payload), payload.getMeta());
    }

    @Operation(summary = "Streaming AI chat", description = "Stream an AI response")
    @PostMapping(value = "/ai/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatPayload payload) {
        return aiGatewayService.stream(resolveScene(payload), resolvePrompt(payload), payload.getMeta());
    }

    @Operation(summary = "AI image generation", description = "Generate image URLs or base64 images for an AI scene")
    @PostMapping("/ai/image")
    public Map<String, Object> generateImage(@RequestBody ChatPayload payload) {
        AiScene scene = resolveScene(payload);
        List<String> images = aiGatewayService.generateImages(scene, resolvePrompt(payload), payload.getMeta());

        Map<String, Object> result = new HashMap<>();
        result.put("scene", scene.name());
        result.put("images", images);
        return result;
    }

    @GetMapping("/ai/config/status")
    public Map<String, Object> configStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("provider", "spring-ai-openai-compatible");
        result.put("baseUrlConfigured", baseUrl != null && !baseUrl.trim().isEmpty());
        result.put("apiKeyConfigured", apiKey != null && !apiKey.trim().isEmpty());
        result.put("apiKeyLength", apiKey == null ? 0 : apiKey.trim().length());
        result.put("chatModelConfigured", chatModel != null && !chatModel.trim().isEmpty());
        result.put("scenes", Arrays.stream(AiScene.values())
                .map(this::sceneStatus)
                .collect(Collectors.toList()));
        return result;
    }

    private Map<String, Object> sceneStatus(AiScene scene) {
        AiGatewayProperties.SceneProperties properties =
                aiGatewayProperties.getScenes().get(scene.getTemplateKey());

        Map<String, Object> status = new HashMap<>();
        status.put("name", scene.name());
        status.put("templateKey", scene.getTemplateKey());
        status.put("streamingPreferred", scene.isStreamingPreferred());
        status.put("template", properties == null ? "" : properties.getTemplate());
        status.put("provider", properties == null ? "" : properties.getProvider());
        status.put("model", properties == null ? "" : properties.getModel());
        status.put("output", properties == null ? "" : properties.getOutput());
        status.put("temperature", properties == null ? null : properties.getTemperature());
        status.put("topP", properties == null ? null : properties.getTopP());
        status.put("maxTokens", properties == null ? null : properties.getMaxTokens());
        return status;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", e.getStatusCode().value());
        body.put("message", e.getReason() == null ? "AI service request failed" : e.getReason());
        return ResponseEntity.status(e.getStatusCode()).body(body);
    }

    private AiScene resolveScene(ChatPayload payload) {
        if (payload == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "payload is required");
        }
        String scene = payload.getScene();
        if (scene == null || scene.trim().isEmpty()) {
            scene = payload.getAgent();
        }
        try {
            return AiScene.from(scene);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private String resolvePrompt(ChatPayload payload) {
        if (payload == null || payload.getPrompt() == null || payload.getPrompt().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "prompt is required");
        }
        return payload.getPrompt();
    }
}
