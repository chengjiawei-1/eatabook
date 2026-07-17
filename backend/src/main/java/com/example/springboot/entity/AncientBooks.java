package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class AncientBooks {
    @TableId(type = IdType.AUTO)
    private Integer bookId;
    private String bookName;
    private String dynasty;
    private String author;
    private String bookCategory;
    private String intro;
    private Integer nourishCount;
    private String bookCoverUrl;
    private Date updateTime;
    private Integer status;
    private Integer viewCount;
}