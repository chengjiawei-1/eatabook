package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dish_ingredient_rel")
public class DishIngredientRel {
    @TableId(value = "rel_id", type = IdType.AUTO)
    private Integer relId;
    private Integer dishId;
    private Integer ingredientId;
    private String usageDetail;
}