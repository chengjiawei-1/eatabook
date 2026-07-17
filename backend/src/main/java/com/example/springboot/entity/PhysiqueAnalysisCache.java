package com.example.springboot.entity;

import lombok.Data;
import java.util.Date;

@Data
public class PhysiqueAnalysisCache {
    private Long id;
    private Integer userId;
    private String analysisContent;
    private Date lastTestTime;
    private Integer testCount;
    private Date createdAt;
    private Date updatedAt;
}