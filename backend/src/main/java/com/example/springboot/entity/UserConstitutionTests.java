package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserConstitutionTests {
    private Integer testId;
    private Integer userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime testTime;
    private String constitutionScores;
    private String constitutionConvertScores;
    private String mainConstitution;
    private String subConstitution;
    private String suggest;
    private String plan;
    private String region;
    private String ageAvoid;
    private String ageFocus;
    private String foodStaple;
    private String foodVeg;
    private String foodMeat;
    private String daily;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}