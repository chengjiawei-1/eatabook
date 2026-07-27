package com.example.springboot.service;

import com.example.springboot.ai.AiPromptTemplateService;
import com.example.springboot.ai.AiScene;
import com.example.springboot.config.AiGatewayProperties;
import com.example.springboot.rag.RagSearchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AiGatewayService {

    private static final Logger log = LoggerFactory.getLogger(AiGatewayService.class);

    private final ChatClient chatClient;
    private final ImageModel imageModel;
    private final AiGatewayProperties properties;
    private final AiPromptTemplateService promptTemplateService;
    private final ObjectMapper objectMapper;
    private final RagSearchService ragSearchService;
    private final AiRequestLimiter aiRequestLimiter;
    private final LaojiToolService laojiToolService;
    private final OpenAiCompatibleChatService openAiCompatibleChatService;

    @Value("${app.laoji.rag-top-k:5}")
    private int laojiRagTopK;

    @Value("${app.laoji.max-context-chars:6000}")
    private int laojiMaxContextChars;

    @Value("${app.laoji.rag-query-expansion-enabled:false}")
    private boolean laojiRagQueryExpansionEnabled;

    public AiGatewayService(ChatClient.Builder chatClientBuilder,
                            ObjectProvider<ImageModel> imageModelProvider,
                            AiGatewayProperties properties,
                            AiPromptTemplateService promptTemplateService,
                            ObjectMapper objectMapper,
                            RagSearchService ragSearchService,
                            AiRequestLimiter aiRequestLimiter,
                            LaojiToolService laojiToolService,
                            OpenAiCompatibleChatService openAiCompatibleChatService) {
        this.chatClient = chatClientBuilder.build();
        this.imageModel = imageModelProvider.getIfAvailable();
        this.properties = properties;
        this.promptTemplateService = promptTemplateService;
        this.objectMapper = objectMapper;
        this.ragSearchService = ragSearchService;
        this.aiRequestLimiter = aiRequestLimiter;
        this.laojiToolService = laojiToolService;
        this.openAiCompatibleChatService = openAiCompatibleChatService;
    }

    public String chat(AiScene scene, String prompt) {
        return chat(scene, prompt, null);
    }

    public String chat(AiScene scene, String prompt, Map<String, Object> meta) {
        return chat(scene, prompt, meta, null);
    }

    public String chat(AiScene scene, String prompt, Map<String, Object> meta, Integer currentUserId) {
        try {
            String renderedPrompt = buildPrompt(scene, prompt, meta, currentUserId);
            String content = aiRequestLimiter.execute(() -> {
                if (usesDirectProvider(scene)) {
                    return openAiCompatibleChatService.call(scene, renderedPrompt);
                }
                return withSceneOptions(chatClient.prompt(), scene)
                        .user(renderedPrompt)
                        .call()
                        .content();
            });
            return sanitizeOutput(scene, content);
        } catch (RuntimeException e) {
            throw toResponseStatusException(e);
        }
    }

    public Flux<String> stream(AiScene scene, String prompt) {
        return stream(scene, prompt, null);
    }

    public Flux<String> stream(AiScene scene, String prompt, Map<String, Object> meta) {
        return stream(scene, prompt, meta, null);
    }

    public Flux<String> stream(AiScene scene, String prompt, Map<String, Object> meta, Integer currentUserId) {
        long startedAt = System.currentTimeMillis();
        AtomicInteger chunkCount = new AtomicInteger();
        String promptPreview = previewPrompt(prompt);

                return aiRequestLimiter.executeFlux(() -> {
                    String renderedPrompt = buildPrompt(scene, prompt, meta, currentUserId);
                    if (usesDirectProvider(scene)) {
                        return openAiCompatibleChatService.stream(scene, renderedPrompt);
                    }
                    return withSceneOptions(chatClient.prompt(), scene)
                            .user(renderedPrompt)
                            .stream()
                            .content();
                })
                .doOnSubscribe(subscription -> log.info(
                        "AI stream started scene={} userId={} prompt={}",
                        scene.getTemplateKey(),
                        currentUserId,
                        promptPreview
                ))
                .map(content -> sanitizeOutput(scene, content))
                .doOnNext(content -> {
                    if (hasText(content)) {
                        chunkCount.incrementAndGet();
                    }
                })
                .doOnComplete(() -> log.info(
                        "AI stream completed scene={} chunks={} elapsedMs={}",
                        scene.getTemplateKey(),
                        chunkCount.get(),
                        System.currentTimeMillis() - startedAt
                ))
                .doOnError(error -> log.warn(
                        "AI stream failed scene={} chunks={} elapsedMs={}",
                        scene.getTemplateKey(),
                        chunkCount.get(),
                        System.currentTimeMillis() - startedAt,
                        error
                ))
                .onErrorResume(error -> Flux.just(toStreamErrorMessage(scene, error)))
                .concatWithValues("[DONE]");
    }

    public List<String> generateImages(AiScene scene, String prompt, Map<String, Object> meta) {
        if (scene == AiScene.CULINARY_STEP_IMAGE) {
            return generateArkImages(scene, prompt, meta);
        }

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

    private List<String> generateArkImages(AiScene scene, String prompt, Map<String, Object> meta) {
        AiGatewayProperties.ImageProperties imageProperties = properties.getImage();
        if (!hasText(imageProperties.getApiKey())) {
            throw new IllegalStateException("ARK_API_KEY is not configured");
        }

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", resolveImageModel(scene));
        payload.put("prompt", buildPrompt(scene, prompt, meta));
        payload.put("sequential_image_generation", imageProperties.getSequentialImageGeneration());

        ObjectNode sequentialOptions = objectMapper.createObjectNode();
        sequentialOptions.put("max_images", resolveImageMaxImages(meta));
        payload.set("sequential_image_generation_options", sequentialOptions);

        payload.put("response_format", imageProperties.getResponseFormat());
        payload.put("size", imageProperties.getSize());
        payload.put("stream", imageProperties.isStream());
        payload.put("watermark", imageProperties.isWatermark());

        try {
            String body = RestClient.create()
                    .post()
                    .uri(imageGenerationUrl(imageProperties.getBaseUrl()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + imageProperties.getApiKey().trim())
                    .body(payload)
                    .retrieve()
                    .body(String.class);
            return extractImageUrls(body);
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    appendUpstreamDetail("火山方舟生图调用失败：" + e.getStatusCode(), e.getResponseBodyAsString()),
                    e
            );
        }
    }

    public byte[] synthesizeSpeech(String text, Map<String, Object> meta) {
        if (!properties.getAudio().isEnabled()) {
            throw new UnsupportedOperationException("AI speech is not enabled");
        }
        throw new UnsupportedOperationException("AI speech gateway is not implemented yet");
    }

    private String buildPrompt(AiScene scene, String prompt, Map<String, Object> meta) {
        return buildPrompt(scene, prompt, meta, null);
    }

    private String buildPrompt(AiScene scene, String prompt, Map<String, Object> meta, Integer currentUserId) {
        if (scene == AiScene.DIET_THERAPY_QA) {
            return promptTemplateService.render(scene, prompt, withLaojiRagContext(prompt, meta, currentUserId));
        }
        return promptTemplateService.render(scene, prompt, meta);
    }

    private Map<String, Object> withLaojiRagContext(String prompt, Map<String, Object> meta, Integer currentUserId) {
        LaojiToolService.LaojiDecision decision = laojiToolService.decide(prompt);
        log.info("Laoji decision source={} categories={} needsKnowledgeContext={}",
                decision.source(),
                decision.categories(),
                decision.needsKnowledgeContext());
        Map<String, Object> augmented = new java.util.HashMap<>();
        if (meta != null) {
            augmented.putAll(meta);
        }
        augmented.put("context", decision.needsKnowledgeContext() ? retrieveLaojiContext(prompt) : "");
        augmented.put("tools", laojiToolService.collectToolContext(currentUserId, prompt, meta, decision));
        augmented.put("statusHint", laojiStatusHint());
        return augmented;
    }

    private String retrieveLaojiContext(String prompt) {
        if (!hasText(prompt)) {
            return "";
        }

        LinkedHashSet<String> contexts = new LinkedHashSet<>();
        for (String query : laojiQueries(prompt)) {
            try {
                List<Document> documents = ragSearchService.search(query, Math.max(1, laojiRagTopK), 0.0);
                for (Document document : documents) {
                    String formatted = formatRagDocument(document);
                    if (hasText(formatted)) {
                        contexts.add(formatted);
                    }
                }
            } catch (RuntimeException ignored) {
                // 老己聊天不应因为知识库临时不可用而整体失效。
            }
        }

        StringBuilder builder = new StringBuilder();
        int maxChars = Math.max(1000, laojiMaxContextChars);
        for (String context : contexts) {
            if (builder.length() + context.length() + 6 > maxChars) {
                break;
            }
            if (!builder.isEmpty()) {
                builder.append("\n---\n");
            }
            builder.append(context);
        }
        return builder.toString();
    }

    private List<String> laojiQueries(String prompt) {
        String text = prompt.trim();
        if (!laojiRagQueryExpansionEnabled) {
            return List.of(text);
        }
        return List.of(
                text,
                "中医测试九大体质特点 九大体质调理方案 " + text,
                "地域饮食信息 地域 气候 饮食 " + text,
                "菜品 食谱 随园食单 饮馔服食笺 " + text,
                "食材 古籍 功效 性味 " + text,
                "营养规划 膳食配比 肠胃负担 " + text
        );
    }

    private String formatRagDocument(Document document) {
        if (document == null || !hasText(document.getText())) {
            return "";
        }
        String source = String.valueOf(document.getMetadata().getOrDefault("source", ""));
        return "source=" + source + "\n" + document.getText();
    }

    private String laojiStatusHint() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour >= 23 || hour < 5) {
            return "深夜时分：老己，放下手机，咱们梦里见。";
        }
        if (hour >= 11 && hour <= 13) {
            return "午餐时间：老己，先把自己喂饱，再谈别的。";
        }
        if (hour >= 18 && hour <= 20) {
            return "晚间时分：老己，今天已经够努力了，吃点稳当的。";
        }
        return "日常陪伴：给老己点时间，先缓一口气。";
    }

    private String resolveImageModel(AiScene scene) {
        AiGatewayProperties.SceneProperties sceneProperties = properties.getScenes().get(scene.getTemplateKey());
        if (sceneProperties != null && hasText(sceneProperties.getModel())) {
            return sceneProperties.getModel().trim();
        }
        return properties.getImage().getModel();
    }

    private int resolveImageMaxImages(Map<String, Object> meta) {
        Integer configured = properties.getImage().getMaxImages();
        int maxImages = configured == null || configured <= 0 ? 4 : configured;
        if (meta != null && meta.get("maxImages") instanceof Number number) {
            maxImages = number.intValue();
        }
        return Math.max(1, Math.min(maxImages, 10));
    }

    private String imageGenerationUrl(String baseUrl) {
        String resolvedBaseUrl = hasText(baseUrl)
                ? baseUrl.trim()
                : "https://ark.cn-beijing.volces.com/api/v3";
        return resolvedBaseUrl.replaceAll("/+$", "") + "/images/generations";
    }

    private List<String> extractImageUrls(String body) {
        LinkedHashSet<String> urls = new LinkedHashSet<>();
        if (!hasText(body)) {
            return List.of();
        }

        for (String line : body.split("\\R")) {
            String text = line.trim();
            if (text.startsWith("data:")) {
                text = text.substring(5).trim();
            }
            if (!hasText(text) || "[DONE]".equals(text)) {
                continue;
            }
            collectUrlsFromJson(text, urls);
        }

        if (urls.isEmpty()) {
            collectUrlsFromJson(body.trim(), urls);
            collectUrlsByPattern(body, urls);
        }
        return List.copyOf(urls);
    }

    private void collectUrlsFromJson(String text, Set<String> urls) {
        try {
            collectUrls(objectMapper.readTree(text), urls);
        } catch (Exception ignored) {
            collectUrlsByPattern(text, urls);
        }
    }

    private void collectUrls(JsonNode node, Set<String> urls) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String name = field.getKey().toLowerCase();
                JsonNode value = field.getValue();
                if ((name.equals("url") || name.endsWith("_url") || name.equals("image_url"))
                        && value.isTextual()
                        && isImageValue(value.asText())) {
                    urls.add(value.asText());
                }
                collectUrls(value, urls);
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                collectUrls(item, urls);
            }
        }
    }

    private void collectUrlsByPattern(String text, Set<String> urls) {
        Pattern pattern = Pattern.compile("(https?://[^\\s\\\"']+|data:image/[^\\s\\\"']+)");
        Matcher matcher = pattern.matcher(text.replace("\\/", "/"));
        while (matcher.find()) {
            urls.add(matcher.group(1));
        }
    }

    private boolean isImageValue(String value) {
        return value != null && (value.startsWith("http://")
                || value.startsWith("https://")
                || value.startsWith("data:image/"));
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

    private boolean usesDirectProvider(AiScene scene) {
        AiGatewayProperties.SceneProperties sceneProperties = properties.getScenes().get(scene.getTemplateKey());
        if (sceneProperties == null || !hasText(sceneProperties.getProvider())) {
            return false;
        }
        String provider = sceneProperties.getProvider().trim();
        return !"siliconflow".equalsIgnoreCase(provider)
                && !"volcengine-ark".equalsIgnoreCase(provider);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String previewPrompt(String prompt) {
        if (!hasText(prompt)) {
            return "";
        }
        String normalized = prompt.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 120 ? normalized : normalized.substring(0, 120) + "...";
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

    private String toStreamErrorMessage(AiScene scene, Throwable error) {
        WebClientResponseException webClientError = findWebClientResponseException(error);
        if (webClientError != null && webClientError.getStatusCode().value() == 429) {
            if (scene == AiScene.DIET_THERAPY_QA) {
                return appendUpstreamDetail("老己啊，上游模型这会儿太忙了。咱先歇一小会儿，等几十秒再来找我唠。", webClientError);
            }
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

    private String appendUpstreamDetail(String message, String body) {
        String detail = normalizeUpstreamDetail(body);
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
