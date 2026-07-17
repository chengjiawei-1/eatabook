package com.example.springboot.controller;


import com.example.springboot.entity.SideBarData;
import com.example.springboot.service.SideBarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sidebar")
@CrossOrigin
@Tag(name = "SidebarController", description = "帖子侧边栏相关接口")
public class SidebarController {

    @Autowired
    private SideBarService sideBarService;

    //获取侧边栏数据
    @Operation(summary = "获取侧边栏数据")
    @GetMapping
    public SideBarData getSidebarData() {
        return sideBarService.getSidebarData();
    }

    //获取各分类帖子数
    @Operation(summary = "获取各分类帖子数")
    @GetMapping("/category-counts")
    public List<Map<String, Object>> getCategoryCounts() {
        return sideBarService.getCategoryCounts();
    }

}
