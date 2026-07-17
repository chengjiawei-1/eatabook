package com.example.springboot.service;

import com.example.springboot.entity.FoodIngredients;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FoodIngredientsService {
    PageInfo<FoodIngredients> findIngredientsByPage(Integer pageNo, Integer pageSize,Integer chapterId);
    List<FoodIngredients> findIngredientsByChapterId(Integer chapter_id);
    List<FoodIngredients> findIngredientsByDishId(Integer dish_id);
    void importIngredientsByExcel(MultipartFile file);
    List<FoodIngredients>findAll();
    int deleteIngredients(Integer ingredientId);
    int updateIngredients(Integer ingredientId, FoodIngredients foodIngredients);
}
