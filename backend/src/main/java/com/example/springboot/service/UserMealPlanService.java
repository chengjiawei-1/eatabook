package com.example.springboot.service;

import com.example.springboot.entity.UserMealPlan;

import java.util.Map;

public interface UserMealPlanService {
    Map<String, Object> getMealPlan(Integer userId, String currentConstitution);
    void savePlan(Integer userId, String planJson, String constitution);
}