package com.example.springboot.excel.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DishExcelDTO {
    @ExcelProperty("菜品名称")
    private String dishName;
    @ExcelProperty("出处")
    private String dishSource;
    @ExcelProperty("适宜体质")
    private String suitableConstitution;
    @ExcelProperty("不适宜体质")
    private String unsuitableConstitution;
    @ExcelProperty("古籍原文")
    private String ancientCitation;
    @ExcelProperty("食物形态")
    private String kind;

    @ExcelProperty("适合春季")
    private String seasonSpringStr;
    @ExcelProperty("适合夏季")
    private String seasonSummerStr;
    @ExcelProperty("适合秋季")
    private String seasonAutumnStr;
    @ExcelProperty("适合冬季")
    private String seasonWinterStr;

    @ExcelProperty("功效1")
    private String effectTag1;
    @ExcelProperty("功效2")
    private String effectTag2;
    @ExcelProperty("功效3")
    private String effectTag3;
    @ExcelProperty("功效4")
    private String effectTag4;
    @ExcelProperty("功效5")
    private String effectTag5;
}