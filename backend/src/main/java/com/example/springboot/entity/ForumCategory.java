package com.example.springboot.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ForumCategory {
    private Integer categoryId;
    private String categoryCode;
    private String categoryName;
    private String intro;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
