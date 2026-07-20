package com.example.springboot.service;

import java.util.List;

public interface CulinaryImageService {

    List<String> processAndGenerate(String dishName, List<String> stepsText);

    List<String> processAndGenerate(String dishName, String sourceText, List<String> stepsText, Integer maxImages);
}
