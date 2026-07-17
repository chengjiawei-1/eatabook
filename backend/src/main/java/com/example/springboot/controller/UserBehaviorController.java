package com.example.springboot.controller;

import com.example.springboot.service.UserBehaviorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "UserBehaviorController", description = "User behavior APIs")
public class UserBehaviorController {

    @Autowired
    private UserBehaviorService userBehaviorService;

    @Operation(summary = "Log dish view behavior")
    @PostMapping("/events/dish-views")
    public Map<String, Object> logClickView(@RequestBody Map<String, Object> params,
                                            HttpServletRequest request) {
        try {
            Long userId = ((Number) params.get("userId")).longValue();
            Long dishId = ((Number) params.get("dishId")).longValue();

            String sessionId = userBehaviorService.logClickView(userId, dishId, request);

            return Map.of(
                    "success", true,
                    "message", "click behavior logged",
                    "sessionId", sessionId
            );
        } catch (Exception e) {
            log.error("Failed to log click behavior", e);
            return Map.of("success", false, "message", "failed to log behavior");
        }
    }

    @Operation(summary = "Log dish favorite behavior")
    @PostMapping("/events/dish-favorites")
    public Map<String, Object> logFavoriteDish(@RequestBody Map<String, Object> params,
                                               HttpServletRequest request) {
        try {
            Long userId = ((Number) params.get("userId")).longValue();
            Long dishId = ((Number) params.get("dishId")).longValue();
            String refererSessionId = (String) params.get("refererSessionId");

            userBehaviorService.logFavoriteDish(userId, dishId, refererSessionId, request);

            return Map.of("success", true, "message", "favorite behavior logged");
        } catch (Exception e) {
            log.error("Failed to log favorite behavior", e);
            return Map.of("success", false, "message", "failed to log behavior");
        }
    }

    @Operation(summary = "Get seasonal recommendation stats")
    @GetMapping("/stats/seasonal-recommendations")
    public Map<String, Object> getHomeRecommendationAnalysis() {
        try {
            Map<String, Object> analysis = userBehaviorService.getHomeRecommendationAnalysis();
            return Map.of("success", true, "data", analysis);
        } catch (Exception e) {
            log.error("Failed to get seasonal recommendation stats", e);
            return Map.of("success", false, "message", "failed to get data");
        }
    }
}
