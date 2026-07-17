package com.example.springboot.req;

import lombok.Data;

@Data
public class ConstitutionTestReq {
    private Integer userId;
    private String constitutionScores;
    private String constitutionConvertScores;
    private String mainConstitution;
    private String subConstitution;
    private String suggest;
    private Boolean needFoodRecommendation = true;
    private String plan;
    private String region;
    private String ageAvoid;
    private String ageFocus;
    private String foodStaple;
    private String foodVeg;
    private String foodMeat;
    private String daily;
}