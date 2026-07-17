package com.example.springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "dify")
public class DifyConfig {
    private String baseUrl;
    private Map<String,AgentInfo>agents;

    @Data
    public static class AgentInfo {
        private String apiKey;
        private String prefix="";
    }

}
