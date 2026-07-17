package com.example.springboot.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class UserCollections {
    private Integer collectionId;
    private Integer userId;
    private String targetType;
    private Integer targetId;
    private LocalDateTime collectionTime;
    private LocalDateTime updateTime;
}