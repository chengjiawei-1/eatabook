package com.example.springboot.dto;

import lombok.Data;
import java.util.List;

@Data
public class CulinaryRequest {
    //接收Dify生成的造法全文
    private String dishName;
    private List<String> stepsText; // 修改这里
}