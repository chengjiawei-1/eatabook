package com.example.springboot.ai;

import java.util.Arrays;
import java.util.Locale;

public enum AiScene {
    ANCIENT_TEXT_TRANSLATION("ancient-text-translation", false, "translator"),
    CONSTITUTION_ANALYSIS("constitution-analysis", false, "suggester"),
    DIET_THERAPY_PLAN("diet-therapy-plan", true, "projecter"),
    CONSTITUTION_CHANGE_ANALYSIS("constitution-change-analysis", true, "changeanalysis"),
    DIET_THERAPY_QA("diet-therapy-qa", true, "laoji"),
    CULINARY_IMAGE_STORYBOARD("culinary-image-storyboard", false, "culinary-storyboard", "storyboard"),
    CULINARY_STEP_IMAGE("culinary-step-image", false, "text-to-image", "image");

    private final String templateKey;
    private final boolean streamingPreferred;
    private final String[] aliases;

    AiScene(String templateKey, boolean streamingPreferred, String... aliases) {
        this.templateKey = templateKey;
        this.streamingPreferred = streamingPreferred;
        this.aliases = aliases;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public boolean isStreamingPreferred() {
        return streamingPreferred;
    }

    public String[] getAliases() {
        return aliases.clone();
    }

    public static AiScene from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("scene or agent is required");
        }

        String raw = value.trim();
        String normalized = normalize(raw);

        for (AiScene scene : values()) {
            if (normalize(scene.name()).equals(normalized)
                    || normalize(scene.templateKey).equals(normalized)) {
                return scene;
            }
            if (Arrays.stream(scene.aliases).anyMatch(alias -> normalize(alias).equals(normalized))) {
                return scene;
            }
        }

        throw new IllegalArgumentException("Unknown AI scene: " + value);
    }

    private static String normalize(String value) {
        return value.trim()
                .replace("-", "_")
                .toUpperCase(Locale.ROOT);
    }
}
