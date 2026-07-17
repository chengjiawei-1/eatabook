package com.example.springboot.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserFollow {
    private Integer id;
    private Integer followerId;
    private Integer followingId;
    private LocalDateTime createTime;
}