package com.example.springboot.service;

import com.example.springboot.entity.Dishes;
import com.example.springboot.entity.FoodIngredients;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DishesService {
    PageInfo<Dishes> findDishesByPage(Integer pageNo, Integer pageSize, Integer chapterId);
    List<Dishes> findAllDishes();
    List<Dishes> findDishesByChapterId(Integer chapter_id);
    Dishes findDishesByDishesId(Integer dish_id);
    List<Dishes> filterDishes(String kind, String season);
    PageInfo<Dishes> findFilterDishesByPage(Integer pageNo, Integer pageSize,String kind, String season);
    PageInfo<Dishes> searchDishes(String keyword,String kind, String season,Integer pageNo, Integer pageSize);
    void importDishesByExcel(MultipartFile file);
    void deleteDishes(Integer dish_id);
    void updateDishes(Integer dish_id, Dishes dishes);
}


