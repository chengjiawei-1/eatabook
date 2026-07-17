package com.example.springboot.entity;

import lombok.Data;

@Data
public class CardData {
    private String title;
    private String icon;
    private Integer value;
    public CardData(String title, String icon, Integer value) {
        this.title = title;
        this.icon = icon;
        this.value = value;
    }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
}
