package com.example.springboot.VO;

import lombok.Data;

@Data
public class DishVideoVO {
    private Long id;
    private String title;
    private String coverUrl;
    private String videoUrl;
    private String duration;
    private String author;
    private Integer price;
    private Boolean isUnlocked;
}