package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.util.Date;

@Data
public class Dishes {
    @TableId(value = "dish_id", type = IdType.AUTO)
    private Integer dishId;
    private String dishName;
    private String dishSource;
    private String effectTag1;
    private String effectTag2;
    private String effectTag3;
    private String effectTag4;
    private String effectTag5;
    private String suitableConstitution;
    private String unsuitableConstitution;
    private String ancientCitation;
    private Integer seasonSpring;
    private Integer seasonSummer;
    private Integer seasonAutumn;
    private Integer seasonWinter;
    private String kind;
    private String dishImgUrl;
    private Date updateTime;
    private Integer collectCount;
}
