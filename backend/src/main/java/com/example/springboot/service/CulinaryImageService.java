package com.example.springboot.service;

import java.util.List;

public interface CulinaryImageService {
    //处理Dify传来的造法文本
    List<String> processAndGenerate(String dishName, List<String> stepsText);
}