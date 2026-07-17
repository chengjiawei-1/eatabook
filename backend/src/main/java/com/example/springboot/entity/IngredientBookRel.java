package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ingredient_book_rel")
public class IngredientBookRel {
    @TableId(value = "rel_id", type = IdType.AUTO)
    private Integer relId;
    private Integer ingredientId;
    private Integer bookId;
    private Integer chapterId;
}