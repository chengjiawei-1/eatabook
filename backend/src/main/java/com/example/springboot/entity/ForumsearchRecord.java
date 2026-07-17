package com.example.springboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumsearchRecord {
    private Long searchId;
    private Integer userId;
    private String searchContent;
    private LocalDateTime searchTime;
    private LocalDateTime updateTime;
}
