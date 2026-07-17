package com.example.springboot.service;

import java.util.List;
import java.util.Map;

public interface DishRecommendService {
    //根据用户ID推荐菜品，并计算匹配度
    List<Map<String, Object>> recommendDishes(Integer userId);

}
