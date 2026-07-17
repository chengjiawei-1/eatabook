package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("forum_post_favorite")
public class ForumPostFavorite {
    private Integer Id;
    private Integer postId;
    private Integer userId;
    private Object createTime;
}

