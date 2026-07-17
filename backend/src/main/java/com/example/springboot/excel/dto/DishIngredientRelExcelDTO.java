package com.example.springboot.excel.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DishIngredientRelExcelDTO {

    @ExcelProperty("菜品名称")
    private String dishName;

    @ExcelProperty("食材名称")
    private String ingredientName;

    @ExcelProperty("用量说明")
    private String usageDetail;
}