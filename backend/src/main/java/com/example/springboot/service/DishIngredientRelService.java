package com.example.springboot.service;

import com.example.springboot.entity.DishIngredientRel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DishIngredientRelService {
    void importDishIngredientRel(MultipartFile file);
    List<DishIngredientRel> findall();
    int deleteByDishId(Integer dishId);
    int deleteByIngredientId(Integer ingredientId);
    int deleteByRelId(Integer relId);
}

