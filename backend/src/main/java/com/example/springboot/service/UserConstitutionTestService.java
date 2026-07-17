package com.example.springboot.service;

import com.example.springboot.entity.UserConstitutionTests;

import com.example.springboot.req.ConstitutionTestReq;
import com.example.springboot.res.ConstitutionTestRes;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface UserConstitutionTestService {
    ConstitutionTestRes submitTest(ConstitutionTestReq req);
    List<UserConstitutionTests> getHistory(Integer userId);
    List<UserConstitutionTests> selectByUserIdForArgent(Integer userId);
    PageInfo<UserConstitutionTests> getHistoryByPage(Integer pageNo, Integer pageSize, Integer userId);
    Integer totalcount();
    Integer todaycount();
    Integer yesterdaycount();
}