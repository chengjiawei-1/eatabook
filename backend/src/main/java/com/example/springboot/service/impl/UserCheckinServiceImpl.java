package com.example.springboot.service.impl;

import com.example.springboot.entity.UserCheckin;
import com.example.springboot.mapper.UserCheckinMapper;
import com.example.springboot.service.UserCheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserCheckinServiceImpl implements UserCheckinService {
    @Autowired
    private UserCheckinMapper userCheckinMapper;
    @Override
    public int insertCheckin(UserCheckin userCheckin) {
        return userCheckinMapper.insertCheckin(userCheckin);
    }

    @Override
    public UserCheckin findLatestByUserId(Integer userId) {
        return userCheckinMapper.findLatestByUserId(userId);
    }

    @Override
    public List<LocalDate> findCheckinDatesInRange(Integer userId, LocalDate startDate, LocalDate endDate) {
        return userCheckinMapper.findCheckinDatesInRange(userId, startDate, endDate);
    }

    @Override
    public int countTodayCheckin(Integer userId, LocalDate today) {
        return userCheckinMapper.countTodayCheckin(userId, today);
    }

    @Override
    public Integer totalCheckins() {
        return userCheckinMapper.totalCheckins();
    }
    @Override
    public Integer todayCheckins() {
        return userCheckinMapper.todayCheckins();
    }
    @Override
    public Integer yesterdayCheckins() {
        return userCheckinMapper.yesterdayCheckins();
    }
}
