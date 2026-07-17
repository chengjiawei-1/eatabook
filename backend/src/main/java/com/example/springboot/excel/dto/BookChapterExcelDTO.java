package com.example.springboot.excel.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class BookChapterExcelDTO {

    @ExcelProperty("所属古籍名称")
    private String bookName;

    @ExcelProperty("章节标题")
    private String chapterTitle;

    @ExcelProperty("章节序号")
    private Integer chapterSeq;

    @ExcelProperty("食材数量")
    private Integer ingredientCount;

    @ExcelProperty("菜品数量")
    private Integer dishCount;
}