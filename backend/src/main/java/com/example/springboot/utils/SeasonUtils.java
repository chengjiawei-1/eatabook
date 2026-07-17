package com.example.springboot.utils;

import java.time.LocalDate;
import java.time.Month;

public class SeasonUtils {
    //根据当前日期计算季节
    public static String getCurrentSeason() {
        LocalDate now = LocalDate.now();
        Month month = now.getMonth();

        return switch (month) {
            case MARCH, APRIL, MAY -> "春季";
            case JUNE, JULY, AUGUST -> "夏季";
            case SEPTEMBER, OCTOBER, NOVEMBER -> "秋季";
            case DECEMBER, JANUARY, FEBRUARY -> "冬季";
            default -> "未知";
        };
    }
}
