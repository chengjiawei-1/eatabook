package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("food_ingredients")
public class FoodIngredients {
    @TableId(value = "ingredient_id", type = IdType.AUTO)
    private Integer ingredientId;
    private String ingredientName;
    private String alias;
    private String property;
    private String category;
    private String ancientEffect;
    private String ancientOriginText;
    private Date updateTime;
}