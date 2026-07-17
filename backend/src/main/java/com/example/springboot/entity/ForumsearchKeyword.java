package com.example.springboot.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ForumsearchKeyword {
    private Long id;
    private String keyword;
    private LocalDate statDate;
    private Integer searchCount;
    private LocalDateTime lastSearchTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
