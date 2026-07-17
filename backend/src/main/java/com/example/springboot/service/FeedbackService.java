package com.example.springboot.service;

import com.example.springboot.entity.Feedback;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface FeedbackService {
    int insert(Feedback feedback);

    PageInfo<Feedback> findByPage(Integer pageNo, Integer pageSize);

    List<Feedback> findAll();
}
