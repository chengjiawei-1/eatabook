package com.example.springboot.excel.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class AncientBookExcelDTO {

    @ExcelProperty("古籍名称")
    private String bookName;

    @ExcelProperty("朝代")
    private String dynasty;

    @ExcelProperty("作者")
    private String author;

    @ExcelProperty("古籍分类")
    private String bookCategory;

    @ExcelProperty("简介")
    private String intro;

    @ExcelProperty("食养条目数量")
    private Integer nourishCount;

    @ExcelProperty("封面URL")
    private String bookCoverUrl;
}
