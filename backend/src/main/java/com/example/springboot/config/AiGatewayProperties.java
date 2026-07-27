package com.example.springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "ai.gateway")
public class AiGatewayProperties {

    private Map<String, SceneProperties> scenes = new HashMap<>();
    private Map<String, ProviderProperties> providers = new HashMap<>();
    private ImageProperties image = new ImageProperties();
    private AudioProperties audio = new AudioProperties();

    @Data
    public static class SceneProperties {
        private String template = "";
        private String provider = "";
        private String model = "";
        private String output = "text";
        private Double temperature;
        private Double topP;
        private Integer maxTokens;
    }

    @Data
    public static class ProviderProperties {
        private String apiKey = "";
        private String baseUrl = "";
        private String completionsPath = "/chat/completions";
    }

    @Data
    public static class ImageProperties {
        private String apiKey = "";
        private String baseUrl = "https://ark.cn-beijing.volces.com/api/v3";
        private String model = "doubao-seedream-4-5-251128";
        private Integer count = 1;
        private Integer maxImages = 4;
        private String size = "2K";
        private String responseFormat = "url";
        private boolean stream = true;
        private boolean watermark = true;
        private String sequentialImageGeneration = "auto";
        private Long stepDelayMillis = 1000L;
    }

    @Data
    public static class AudioProperties {
        private boolean enabled = false;
        private String model = "";
        private String voice = "";
    }
}
