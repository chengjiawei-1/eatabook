package com.example.springboot.entity;

import lombok.Data;

import java.util.Date;


@Data
public class UserMealPlan {
    private Integer userId;
    private String planJson;
    private Date generateDate;
    private String baseConstitution;
    private Date updateTime;
}
