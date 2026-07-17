package com.example.springboot.service;

import org.springframework.web.multipart.MultipartFile;

public interface IngredientBookRelService {
    void importRelByExcel(MultipartFile file);
    int deleteByIngredientId(Integer ingredientId);
}