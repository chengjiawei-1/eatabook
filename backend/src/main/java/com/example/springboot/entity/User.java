package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.util.Date;

@Data
public class User {
    private Integer userId;
    private String username;
    private String nativePlace;
    private String password;
    private String phone;
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private LocalDateTime registerTime;
    private String avatarUrl;
    private LocalDateTime updateTime;
    private String role ;
    private LocalDateTime lastLoginTime;
    private LocalDateTime nextTestTime;
    private Integer points = 0;
    private Integer followingCount = 0;
    private Integer followerCount = 0;
    private String content;
    private String city;
}