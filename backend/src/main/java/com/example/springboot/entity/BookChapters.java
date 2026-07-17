package com.example.springboot.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class BookChapters {
    @TableId(value = "chapter_id", type = IdType.AUTO)
    private Integer chapterId;
    private Integer bookId;
    private String chapterTitle;
    private Integer chapterSeq;
    private Integer ingredientCount;
    private Integer dishCount;
    private Date updateTime;
}