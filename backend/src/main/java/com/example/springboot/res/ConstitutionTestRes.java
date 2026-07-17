package com.example.springboot.res;

import lombok.Data;
import java.util.List;

@Data
public class ConstitutionTestRes {
    private Integer testId;
    private String msg;
    private List<FoodRecommendation> foodRecommendations;

    @Data
    public static class FoodRecommendation {
        private Integer dishId;
        private String dishName;
        private String effect;
        private String dishImgUrl;
    }
}