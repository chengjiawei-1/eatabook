package com.example.springboot.ai;

import com.example.springboot.config.AiGatewayProperties;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiPromptTemplateService {

    private static final String DEFAULT_TEMPLATE_PATTERN = "classpath:/ai/prompts/%s.st";
    private static final StTemplateRenderer TEMPLATE_RENDERER = StTemplateRenderer.builder()
            .startDelimiterToken('<')
            .endDelimiterToken('>')
            .build();

    private final AiGatewayProperties properties;
    private final ResourceLoader resourceLoader;

    public AiPromptTemplateService(AiGatewayProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    public String render(AiScene scene, String prompt, Map<String, Object> meta) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("prompt", prompt == null ? "" : prompt);
        if (meta != null) {
            variables.put("meta", meta);
            variables.putAll(meta);
        } else {
            variables.put("meta", Collections.emptyMap());
        }
        return PromptTemplate.builder()
                .resource(resolveTemplate(scene))
                .renderer(TEMPLATE_RENDERER)
                .build()
                .render(variables);
    }

    private Resource resolveTemplate(AiScene scene) {
        String location = null;
        AiGatewayProperties.SceneProperties sceneProperties =
                properties.getScenes().get(scene.getTemplateKey());
        if (sceneProperties != null && sceneProperties.getTemplate() != null) {
            location = sceneProperties.getTemplate().trim();
        }
        if (location == null || location.isEmpty()) {
            location = String.format(DEFAULT_TEMPLATE_PATTERN, scene.getTemplateKey());
        }
        return resourceLoader.getResource(location);
    }
}
