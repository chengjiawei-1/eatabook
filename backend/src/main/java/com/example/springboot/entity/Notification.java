package com.example.springboot.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    private Long id;

    private Long userId;

    private String type;

    private String title;

    private String content;

    private String link;

    private Long relatedId;

    private Boolean readStatus = false;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}
