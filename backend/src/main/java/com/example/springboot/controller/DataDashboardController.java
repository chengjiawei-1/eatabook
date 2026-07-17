package com.example.springboot.controller;

import com.example.springboot.entity.Dishes;
import com.example.springboot.entity.User;
import com.example.springboot.entity.UserConstitutionTests;
import com.example.springboot.service.DataDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Tag(name="DataDashboardController",description="可视化数据相关接口")
@CrossOrigin
@RestController
@RequestMapping("/api/databoard")
public class DataDashboardController {
    @Autowired
    private DataDashboardService dataDashboardService;
    // 获取卡片统计数据
    @Operation(summary = "卡片数据", description = "查询所有古籍数量、总用户数、菜品总数、体质测试数")
    @GetMapping("/cards")
    public List<Map<String, Object>> getStatistics() {
        List<Map<String, Object>> cards = new ArrayList<>();
        // 总用户数卡片
        Map<String, Object> totalUsersCard = new HashMap<>();
        totalUsersCard.put("icon", "fa-users");
        totalUsersCard.put("title", "总用户数");
        totalUsersCard.put("value", dataDashboardService.getTotalUsers());
        cards.add(totalUsersCard);
        // 古籍收藏卡片
        Map<String, Object> booksCard = new HashMap<>();
        booksCard.put("icon", "fa-book");
        booksCard.put("title", "古籍收藏");
        booksCard.put("value", dataDashboardService.getAllBooks());
        cards.add(booksCard);
        // 食疗菜品卡片
        Map<String, Object> dishesCard = new HashMap<>();
        dishesCard.put("icon", "fa-utensils");
        dishesCard.put("title", "食疗菜品");
        dishesCard.put("value", dataDashboardService.getAllDishes());
        cards.add(dishesCard);
        // 体质测试卡片
        Map<String, Object> testsCard = new HashMap<>();
        testsCard.put("icon", "fa-clipboard-list");
        testsCard.put("title", "体质测试");
        testsCard.put("value", dataDashboardService.getTotalTests());
        cards.add(testsCard);

        return cards;
    }

    //排行榜
    @Operation(summary = "排行榜所用菜品查询", description = "查询收藏前八古籍")
    @GetMapping("/dish/rank")
    public List<Map<String, Object>> getTop8Dish() {
        return dataDashboardService.getTop8Dish(8);
    }

    // 环形图-获取所有用户体质分布
    @Operation(summary = "所有用户体质查询", description = "查询所有用户主要体质")
    @GetMapping("/constitution/all")
    public List<Map<String, Object>> getAllConstitutionDistribution() {
        return dataDashboardService.getAllConstitutionDistribution();
    }

    //环形图-获取指定地区用户体质分布
    @Operation(summary = "指定地区用户体质查询", description = "查询指定地区用户主要体质")
    @GetMapping("/constitution/province")
    public List<Map<String, Object>> getConstitutionDistributionByProvince(
            @RequestParam(required = false) String province) {
        return dataDashboardService.getConstitutionDistributionByProvince(province);
    }

    //雷达图-菜品
    @Operation(summary = "雷达图所用数据查询", description = "体质-菜品，匹配度")
    @GetMapping("/radar/dishes")
    public List<Map<String, Object>> findTop5DishesByConstitutionAndSeason(
            @RequestParam(required = false) String season) {
        return dataDashboardService.getTop5DishesByConstitutionAndSeason( season);
    }

    // 全部地图数据
    @Operation(summary = "全国各省用户体质查询", description = "查询各省用户主要体质")
    @GetMapping("/map/all")
    public List<Map<String, Object>> getAllMapData() {
        return dataDashboardService.getAllMapData(); }

    //词云图-获取今日热搜前20关键词
    @Operation(summary = "热搜前20关键词查询", description = "查询今日前20关键词")
    @GetMapping("/keywords")
    public ResponseEntity<List<Map<String, Object>>> getTop20Keywords() {
        try {
            List<Map<String, Object>> topKeywords = dataDashboardService.getTop20Keywords();
            return ResponseEntity.ok(topKeywords);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
