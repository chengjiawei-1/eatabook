package com.example.springboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostImage {
    private Integer imageId;
    private Integer postId;
    private String imageUrl;
    private Integer sortOrder;
    private Integer imageType;
    private Integer status;
    private LocalDateTime createTime;
}
