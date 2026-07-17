package com.example.springboot.service;

import com.example.springboot.entity.Notification;
import java.util.List;

public interface NotificationService {

    void sendNotification(Notification notification);

    List<Notification> getUserNotifications(Long userId, int limit);

    Notification getNotificationById(Long id, Long userId);

    void markAsRead(Long id, Long userId);

    void markAllAsRead(Long userId);

    int getUnreadCount(Long userId);

    void deleteNotification(Long id, Long userId);
}