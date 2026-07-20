package com.example.springboot.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.springboot.ai.AiScene;
import com.example.springboot.config.AiGatewayProperties;
import com.example.springboot.service.AiGatewayService;
import com.example.springboot.service.CulinaryImageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CulinaryImageServiceImpl implements CulinaryImageService {

    private static final String IMAGE_GEN_FAILED = "IMAGE_GEN_FAILED";

    private final AiGatewayService aiGatewayService;
    private final AiGatewayProperties properties;
    private final ObjectMapper objectMapper;

    public CulinaryImageServiceImpl(AiGatewayService aiGatewayService,
                                    AiGatewayProperties properties,
                                    ObjectMapper objectMapper) {
        this.aiGatewayService = aiGatewayService;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<String> processAndGenerate(String dishName, List<String> stepsText) {
        return processAndGenerate(dishName, null, stepsText, null);
    }

    @Override
    public List<String> processAndGenerate(String dishName,
                                           String sourceText,
                                           List<String> stepsText,
                                           Integer maxImages) {
        List<String> imagePrompts = planImagePrompts(dishName, sourceText, stepsText, maxImages);
        return generateImagesFromPrompts(dishName, imagePrompts);
    }

    private List<String> planImagePrompts(String dishName,
                                          String sourceText,
                                          List<String> stepsText,
                                          Integer maxImages) {
        if (hasText(sourceText)) {
            List<String> plannedPrompts = createStoryboardPrompts(dishName, sourceText, stepsText, maxImages);
            if (!plannedPrompts.isEmpty()) {
                return plannedPrompts;
            }
        }
        return fallbackStepPrompts(stepsText, maxImages);
    }

    private List<String> createStoryboardPrompts(String dishName,
                                                 String sourceText,
                                                 List<String> stepsText,
                                                 Integer maxImages) {
        try {
            String storyboard = aiGatewayService.chat(
                    AiScene.CULINARY_IMAGE_STORYBOARD,
                    sourceText,
                    Map.of(
                            "dishName", clean(dishName),
                            "maxImages", resolveMaxImages(maxImages),
                            "legacySteps", joinSteps(stepsText)
                    )
            );
            return parseStoryboardPrompts(storyboard, maxImages);
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<String> generateImagesFromPrompts(String dishName, List<String> imagePrompts) {
        List<String> imageUrls = new ArrayList<>();
        for (String imagePrompt : imagePrompts) {
            try {
                List<String> generated = aiGatewayService.generateImages(
                        AiScene.CULINARY_STEP_IMAGE,
                        imagePrompt,
                        Map.of("dishName", clean(dishName), "step", imagePrompt)
                );
                imageUrls.add(generated.isEmpty() ? IMAGE_GEN_FAILED : generated.get(0));
                waitBetweenSteps();
            } catch (Exception e) {
                imageUrls.add(IMAGE_GEN_FAILED);
            }
        }

        return imageUrls;
    }

    private List<String> parseStoryboardPrompts(String storyboard, Integer maxImages) throws Exception {
        String json = extractJsonArray(storyboard);
        if (!hasText(json)) {
            return List.of();
        }

        JsonNode root = objectMapper.readTree(json);
        if (!root.isArray()) {
            return List.of();
        }

        List<String> prompts = new ArrayList<>();
        for (JsonNode node : root) {
            String prompt = node.isTextual() ? node.asText() : node.path("prompt").asText("");
            if (hasText(prompt)) {
                prompts.add(prompt.trim());
            }
            if (prompts.size() >= resolveMaxImages(maxImages)) {
                break;
            }
        }
        return prompts;
    }

    private String extractJsonArray(String content) {
        if (!hasText(content)) {
            return "";
        }
        String cleanContent = content.trim()
                .replace("```json", "")
                .replace("```JSON", "")
                .replace("```", "")
                .trim();
        int start = cleanContent.indexOf('[');
        int end = cleanContent.lastIndexOf(']');
        if (start < 0 || end <= start) {
            return "";
        }
        return cleanContent.substring(start, end + 1);
    }

    private List<String> fallbackStepPrompts(List<String> stepsText, Integer maxImages) {
        if (stepsText == null || stepsText.isEmpty()) {
            return List.of();
        }
        return stepsText.stream()
                .filter(this::hasText)
                .map(String::trim)
                .limit(resolveMaxImages(maxImages))
                .collect(Collectors.toList());
    }

    private Integer resolveMaxImages(Integer maxImages) {
        int configured = properties.getImage().getMaxImages() == null
                ? 4
                : properties.getImage().getMaxImages();
        int requested = maxImages == null || maxImages <= 0 ? configured : maxImages;
        return Math.max(1, Math.min(requested, configured));
    }

    private String joinSteps(List<String> stepsText) {
        if (stepsText == null || stepsText.isEmpty()) {
            return "";
        }
        return stepsText.stream()
                .filter(this::hasText)
                .map(String::trim)
                .collect(Collectors.joining("\n"));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private void waitBetweenSteps() throws InterruptedException {
        Long delay = properties.getImage().getStepDelayMillis();
        if (delay != null && delay > 0) {
            Thread.sleep(delay);
        }
    }
}
