package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_point_log")
public class UserPointLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer userId;
    private Integer type;
    private Integer amount;
    private Integer balance;
    private String description;
    private LocalDateTime createTime;
}