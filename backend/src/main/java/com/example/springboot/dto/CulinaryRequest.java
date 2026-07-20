package com.example.springboot.dto;

import lombok.Data;

import java.util.List;

@Data
public class CulinaryRequest {
    private String dishName;
    private String sourceText;
    private List<String> stepsText;
    private Integer maxImages;
}
