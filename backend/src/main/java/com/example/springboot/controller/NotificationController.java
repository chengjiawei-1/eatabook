package com.example.springboot.controller;

import com.example.springboot.entity.Notification;
import com.example.springboot.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "NotificationController", description = "用户通知相关接口")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "查询通知列表")
    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "20") int limit) {
        List<Notification> notifications = notificationService.getUserNotifications(userId, limit);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "查询未读通知")
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(@RequestAttribute Long userId) {
        int count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @Operation(summary = "标记特定通知已读")
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @RequestAttribute Long userId) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "标记所有通知已读")
    @PutMapping("/read")
    public ResponseEntity<Void> markAllAsRead(@RequestAttribute Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "删除特定通知")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id, @RequestAttribute Long userId) {
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok().build();
    }
}
