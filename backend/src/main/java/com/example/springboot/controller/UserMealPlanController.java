package com.example.springboot.controller;

import com.example.springboot.service.UserMealPlanService;
import com.example.springboot.service.DietTherapyPlanGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name="UserMealPlanController", description="用户七天食疗计划接口")
@CrossOrigin
@RestController
@RequestMapping("/api/meal-plans")
public class UserMealPlanController {

    @Autowired
    private UserMealPlanService userMealPlanService;

    @Autowired
    private DietTherapyPlanGenerationService dietTherapyPlanGenerationService;

    //获取有效的食疗计划
    @Operation(summary = "获取有效计划", description = "校验日期和体质，命中则返回JSON，否则返回null")
    @GetMapping("/current")
    public Map<String, Object> getMealPlan(
            @RequestParam Integer userId,
            @RequestParam String currentConstitution) {

        return userMealPlanService.getMealPlan(userId, currentConstitution);
    }

    //保存从 AI 生成的新计划
    @Operation(summary = "保存/更新计划", description = "当AI生成新计划后调用此接口存入数据库")
    @PostMapping
    public String savePlan(@RequestBody Map<String, Object> params) {
        try {
            Integer userId = (Integer) params.get("userId");
            String planJson = (String) params.get("planJson");
            String constitution = (String) params.get("constitution");

            userMealPlanService.savePlan(userId, planJson, constitution);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @Operation(summary = "生成七天食疗计划", description = "读取用户资料和最新体质测试，结合 RAG 菜品知识库生成 7 天早餐/午餐/晚餐 dish_id")
    @PostMapping("/generate")
    public Map<String, Object> generatePlan(@RequestBody GenerateMealPlanRequest request) {
        return dietTherapyPlanGenerationService.generate(
                request.userId(),
                request.prompt(),
                request.save(),
                request.topK()
        );
    }

    public record GenerateMealPlanRequest(Integer userId, String prompt, Boolean save, Integer topK) {
    }
}
