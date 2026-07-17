package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ForumPost {
    @EqualsAndHashCode.Include
    private Integer postId;
    private String title;
    private String content;
    private Integer categoryId;
    private Integer userId;
    private String relatedType;
    private Integer relatedId;
    private String coverImage;
    private Integer likeCount = 0;
    private Integer commentCount = 0;
    private Integer favoriteCount = 0;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean liked;
    private Boolean favorited;
    @TableField(exist = false)
    private List<String> images;
}