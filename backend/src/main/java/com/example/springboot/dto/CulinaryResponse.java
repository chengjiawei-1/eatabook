package com.example.springboot.dto;

import lombok.Data;
import java.util.List;

@Data
public class CulinaryResponse {
    private boolean success;
    private List<String> imageUrls;
    private String message;
}