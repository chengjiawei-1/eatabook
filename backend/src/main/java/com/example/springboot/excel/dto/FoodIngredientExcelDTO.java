package com.example.springboot.excel.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class FoodIngredientExcelDTO {
    @ExcelProperty("食材名称")
    private String ingredientName;

    @ExcelProperty("别名")
    private String alias;

    @ExcelProperty("性味")
    private String property;

    @ExcelProperty("分类")
    private String category;

    @ExcelProperty("核心功效")
    private String ancientEffect;

    @ExcelProperty("古籍原文")
    private String ancientOriginText;
}