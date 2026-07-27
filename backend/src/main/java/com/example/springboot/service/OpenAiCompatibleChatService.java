package com.example.springboot.service;

import com.example.springboot.ai.AiScene;
import com.example.springboot.config.AiGatewayProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Service
public class OpenAiCompatibleChatService {

    private final AiGatewayProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiCompatibleChatService(AiGatewayProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public boolean supports(AiScene scene) {
        AiGatewayProperties.SceneProperties sceneProperties = sceneProperties(scene);
        return sceneProperties != null && supportsProvider(sceneProperties.getProvider());
    }

    public boolean supportsProvider(String provider) {
        AiGatewayProperties.ProviderProperties providerProperties = providerProperties(provider);
        return providerProperties != null
                && hasText(providerProperties.getApiKey())
                && hasText(providerProperties.getBaseUrl());
    }

    public String call(AiScene scene, String prompt) {
        AiGatewayProperties.SceneProperties sceneProperties = sceneProperties(scene);
        if (sceneProperties == null) {
            throw new IllegalArgumentException("Missing AI scene configuration: " + scene.getTemplateKey());
        }
        return callProvider(
                sceneProperties.getProvider(),
                sceneProperties.getModel(),
                sceneProperties.getTemperature(),
                sceneProperties.getTopP(),
                sceneProperties.getMaxTokens(),
                prompt
        );
    }

    public String callProvider(String provider,
                               String model,
                               Double temperature,
                               Double topP,
                               Integer maxTokens,
                               String prompt) {
        AiGatewayProperties.ProviderProperties providerProperties = requireProvider(provider);
        ObjectNode payload = requestPayload(model, temperature, topP, maxTokens, prompt, false);
        try {
            String body = RestClient.create()
                    .post()
                    .uri(completionsUrl(providerProperties))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + providerProperties.getApiKey().trim())
                    .body(payload)
                    .retrieve()
                    .body(String.class);
            return extractContent(body);
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI provider request failed: " + e.getStatusCode() + providerDetail(e.getResponseBodyAsString()),
                    e
            );
        }
    }

    public Flux<String> stream(AiScene scene, String prompt) {
        AiGatewayProperties.SceneProperties sceneProperties = sceneProperties(scene);
        if (sceneProperties == null) {
            return Flux.error(new IllegalArgumentException("Missing AI scene configuration: " + scene.getTemplateKey()));
        }
        AiGatewayProperties.ProviderProperties providerProperties = requireProvider(sceneProperties.getProvider());
        ObjectNode payload = requestPayload(
                sceneProperties.getModel(),
                sceneProperties.getTemperature(),
                sceneProperties.getTopP(),
                sceneProperties.getMaxTokens(),
                prompt,
                true
        );

        return Flux.create(sink -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(completionsUrl(providerProperties)))
                        .timeout(Duration.ofMinutes(3))
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .header("Accept", MediaType.TEXT_EVENT_STREAM_VALUE)
                        .header("Authorization", "Bearer " + providerProperties.getApiKey().trim())
                        .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                        .build();

                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                        .whenComplete((response, error) -> {
                            if (error != null) {
                                sink.error(error);
                                return;
                            }
                            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                                sink.error(new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "AI provider stream failed: HTTP " + response.statusCode() + providerDetail(readBody(response.body()))
                                ));
                                return;
                            }
                            readStream(response.body(), sink);
                        });
            } catch (RuntimeException e) {
                sink.error(e);
            }
        });
    }

    private ObjectNode requestPayload(String model,
                                      Double temperature,
                                      Double topP,
                                      Integer maxTokens,
                                      String prompt,
                                      boolean stream) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", hasText(model) ? model.trim() : "deepseek-chat");
        payload.put("stream", stream);
        if (temperature != null) {
            payload.put("temperature", temperature);
        }
        if (topP != null) {
            payload.put("top_p", topP);
        }
        if (maxTokens != null && maxTokens > 0) {
            payload.put("max_tokens", maxTokens);
        }
        ArrayNode messages = payload.putArray("messages");
        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", prompt == null ? "" : prompt);
        return payload;
    }

    private String extractContent(String body) {
        if (!hasText(body)) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            return root.path("choices").path(0).path("message").path("content").asText("");
        } catch (Exception e) {
            return "";
        }
    }

    private void readStream(InputStream body, reactor.core.publisher.FluxSink<String> sink) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null && !sink.isCancelled()) {
                String text = line.trim();
                if (!text.startsWith("data:")) {
                    continue;
                }
                String data = text.substring(5).trim();
                if (!hasText(data)) {
                    continue;
                }
                if ("[DONE]".equals(data)) {
                    sink.complete();
                    return;
                }
                String content = extractDelta(data);
                if (hasText(content)) {
                    sink.next(content);
                }
            }
            sink.complete();
        } catch (Exception e) {
            sink.error(e);
        }
    }

    private String extractDelta(String data) {
        try {
            JsonNode root = objectMapper.readTree(data);
            return root.path("choices").path(0).path("delta").path("content").asText("");
        } catch (Exception e) {
            return "";
        }
    }

    private AiGatewayProperties.SceneProperties sceneProperties(AiScene scene) {
        return properties.getScenes().get(scene.getTemplateKey());
    }

    private AiGatewayProperties.ProviderProperties requireProvider(String provider) {
        AiGatewayProperties.ProviderProperties providerProperties = providerProperties(provider);
        if (providerProperties == null || !hasText(providerProperties.getApiKey()) || !hasText(providerProperties.getBaseUrl())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI provider is not configured: " + provider);
        }
        return providerProperties;
    }

    private AiGatewayProperties.ProviderProperties providerProperties(String provider) {
        if (!hasText(provider)) {
            return null;
        }
        return properties.getProviders().get(provider.trim());
    }

    private String completionsUrl(AiGatewayProperties.ProviderProperties providerProperties) {
        String path = hasText(providerProperties.getCompletionsPath())
                ? providerProperties.getCompletionsPath().trim()
                : "/chat/completions";
        return providerProperties.getBaseUrl().replaceAll("/+$", "") + "/" + path.replaceAll("^/+", "");
    }

    private String providerDetail(String body) {
        if (!hasText(body)) {
            return "";
        }
        String detail = body.replaceAll("\\s+", " ").trim();
        return " - " + (detail.length() <= 200 ? detail : detail.substring(0, 200) + "...");
    }

    private String readBody(InputStream body) {
        if (body == null) {
            return "";
        }
        try {
            return new String(body.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }
}
