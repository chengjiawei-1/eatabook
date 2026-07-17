package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
@Data
@TableName("dish_video")
public class DishVideo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dishId;
    private String title;
    private String coverUrl;
    private String videoUrl;
    private String duration;
    private String author;
    private Integer price;
}

