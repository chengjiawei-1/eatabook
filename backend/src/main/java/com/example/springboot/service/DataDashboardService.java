package com.example.springboot.service;

import com.example.springboot.entity.CardData;
import com.example.springboot.entity.Dishes;
import com.example.springboot.entity.User;
import com.example.springboot.entity.UserConstitutionTests;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public interface DataDashboardService {

    Integer getTotalUsers();
    Integer getTotalTests();
    Integer getAllDishes();
    Integer getAllBooks();
    // 获取首页卡片数据
    List<CardData> getCardData();
    //排行榜
    List<Map<String, Object>> getTop8Dish(@Param("limit") Integer limit);
    //环形图-所有用户体质分布
    List<Map<String, Object>> getAllConstitutionDistribution();
    //环形图-指定地区用户体质分布
    List<Map<String, Object>> getConstitutionDistributionByProvince(
            @Param("province") String province
    );
    //雷达图-相关体质对应应季菜品
    List<Map<String, Object>> getTop5DishesByConstitutionAndSeason(
            @Param("season") String season
    );
    // 地图数据
    List<Map<String, Object>> getMapDataByRegion(
            @Param("region") String region
    );
    // 获取全国地图数据
    List<Map<String, Object>> getAllMapData();
    // 流程调度方法
    void statTodayHotKeywords();
    // 添加实时分词处理方法到接口
    void processSearchContent(String searchContent);
    // 获取今日最热门关键词top20
    List<Map<String, Object>> getTop20Keywords();



}
