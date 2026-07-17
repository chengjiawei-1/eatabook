package com.example.springboot.excel.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class IngredientBookRelExcelDTO {
    @ExcelProperty("食材名称")
    private String ingredientName;

    @ExcelProperty("所属古籍名称")
    private String bookName;

    @ExcelProperty("所属章节标题")
    private String chapterTitle;
}