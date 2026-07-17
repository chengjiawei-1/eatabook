package com.example.springboot.controller;

import com.example.springboot.service.DishRecommendService;
import com.example.springboot.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "DishRecommendController", description = "菜品推荐相关接口")
public class DishRecommendController {

    @Autowired
    private DishRecommendService dishRecommendService;

    // 统一返回结果封装
    private Map<String, Object> resultSuccess(Object data) {
        return resultSuccess(data, "操作成功");
    }

    private Map<String, Object> resultSuccess(Object data, String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", msg);
        map.put("data", data);
        return map;
    }

    private Map<String, Object> resultError(String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 500);
        map.put("msg", msg);
        map.put("data", null);
        return map;
    }

    //解析Token
    private Integer parseToken(String token) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new Exception("Token不能为空");
        }
        String realToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return JwtUtils.parseToken(realToken);
    }

    @Operation(summary = "查询推荐菜品", description = "查询所有推荐菜品")
    @GetMapping("/dishes")
    public Map<String, Object> getRecommendedDishes(@RequestHeader("Authorization") String token) {
        try {
            Integer userId = parseToken(token);

            List<Map<String, Object>> recommendedDishes = dishRecommendService.recommendDishes(userId);

            return resultSuccess(recommendedDishes);
        } catch (Exception e) {
            e.printStackTrace();
            return resultError("获取推荐菜品失败: " + e.getMessage());
        }
    }
}
