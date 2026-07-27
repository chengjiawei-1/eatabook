package com.example.springboot.utils;

import java.util.List;

public final class AiOutputGuard {

    private static final List<String> FAILURE_MARKERS = List.of(
            "AI 服务请求过于频繁",
            "AI 服务调用失败",
            "AI service request failed",
            "AI service call failed",
            "Too Many Requests",
            "TOO_MANY_REQUESTS",
            "rate limiting",
            "System is too busy",
            "Request was rejected",
            "50609",
            "429"
    );

    private AiOutputGuard() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isFailureText(String value) {
        if (!hasText(value)) {
            return false;
        }
        return FAILURE_MARKERS.stream().anyMatch(value::contains);
    }
}
