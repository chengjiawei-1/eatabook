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
    public static class ImageProperties {
        private Integer count = 1;
        private Integer maxImages = 4;
        private String size = "2K";
        private String responseFormat = "url";
        private Long stepDelayMillis = 1000L;
    }

    @Data
    public static class AudioProperties {
        private boolean enabled = false;
        private String model = "";
        private String voice = "";
    }
}
