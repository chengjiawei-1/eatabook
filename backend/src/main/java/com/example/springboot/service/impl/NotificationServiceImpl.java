package com.example.springboot.service.impl;

import com.example.springboot.entity.Notification;
import com.example.springboot.mapper.NotificationMapper;
import com.example.springboot.service.NotificationService;
import com.example.springboot.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationWebSocketHandler webSocketHandler;

    @Override
    @Transactional
    public void sendNotification(Notification notification) {
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }
        if (notification.getReadStatus() == null) {
            notification.setReadStatus(false);
        }
        notificationMapper.insert(notification);
        webSocketHandler.sendNotificationToUser(notification.getUserId(), notification);
    }

    @Override
    public List<Notification> getUserNotifications(Long userId, int limit) {
        return notificationMapper.findByUserId(userId, limit);
    }

    @Override
    public Notification getNotificationById(Long id, Long userId) {
        return notificationMapper.findByIdAndUserId(id, userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long id, Long userId) {
        notificationMapper.markAsRead(id);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long id, Long userId) {
        notificationMapper.deleteByIdAndUserId(id, userId);
    }
}
