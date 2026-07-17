package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.UserCheckin;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface UserCheckinService{
    int insertCheckin(UserCheckin userCheckin);
    UserCheckin findLatestByUserId(Integer userId);
    List<LocalDate> findCheckinDatesInRange(Integer userId,LocalDate startDate,LocalDate endDate);
    int countTodayCheckin(Integer userId,LocalDate today);
    Integer totalCheckins();
    Integer todayCheckins();
    Integer yesterdayCheckins();
}