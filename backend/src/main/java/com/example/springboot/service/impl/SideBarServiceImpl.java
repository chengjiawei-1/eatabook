package com.example.springboot.service.impl;

import com.example.springboot.entity.SideBarData;
import com.example.springboot.mapper.SideBarMapper;
import com.example.springboot.service.SideBarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SideBarServiceImpl implements SideBarService {

    @Autowired
    private SideBarMapper sideBarMapper;

    // 获取侧边栏数据
    @Override
    public SideBarData getSidebarData() {
        SideBarData data = new SideBarData();
        data.setTotalPosts(getTotalPosts());
        data.setTotalUsers(getTotalUsers());
        data.setTotalCategories(getTotalCategories());
        return data;
    }

    // 获取帖子总数
    @Override
    public Integer getTotalPosts() {
        return sideBarMapper.getTotalPosts();
    }

    // 获取用户总数
    @Override
    public Integer getTotalUsers() {
        return sideBarMapper.getTotalUsers();
    }

    // 获取分类总数
    @Override
    public Integer getTotalCategories() {
        return sideBarMapper.getTotalCategories();
    }

    // 获取各分类贴子数统计
    @Override
    public List<Map<String, Object>> getCategoryCounts() {
        return sideBarMapper.getCategoryCounts();
    }

}
