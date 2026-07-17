package com.example.springboot.service.impl;

import com.example.springboot.entity.Feedback;
import com.example.springboot.mapper.FeedbackMapper;
import com.example.springboot.service.FeedbackService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    public int insert(Feedback feedback) {
        return feedbackMapper.insert(feedback);
    }

    @Override
    public PageInfo<Feedback> findByPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<Feedback> list = feedbackMapper.findAllFeedback();
        return new PageInfo<>(list);
    }

    @Override
    public List<Feedback> findAll() {
        return feedbackMapper.findAllFeedback();
    }
}
