package com.example.springboot.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ForumComment {
    private Integer commentId;
    private Integer postId;
    private Integer userId;
    private String content;
    private Integer parentId;
    private Integer replyUserId;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
