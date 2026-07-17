package com.example.springboot.service;

import com.example.springboot.entity.SideBarData;

import java.util.List;
import java.util.Map;

public interface SideBarService {

    SideBarData getSidebarData();

    // 获取帖子总数
    Integer getTotalPosts();

    // 获取用户总数
    Integer getTotalUsers();

    // 获取分类总数
    Integer getTotalCategories();

    // 获取各个类别帖子数
    List<Map<String, Object>> getCategoryCounts();
}
