package com.example.springboot.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserCheckin {
    private Integer id;
    private Integer userId;
    private LocalDate checkinDate;
    private Boolean isRewarded;
    private Integer currentStreak;
    private LocalDate lastCheckinDate;
    private LocalDateTime createdAt;
}