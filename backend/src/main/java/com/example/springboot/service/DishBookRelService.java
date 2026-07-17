package com.example.springboot.service;

import org.springframework.web.multipart.MultipartFile;

public interface DishBookRelService {
    //导入关联关系
    void importRelByExcel(MultipartFile file);
    //删除关联关系
    boolean deleteByDishId(Integer dishId);
}