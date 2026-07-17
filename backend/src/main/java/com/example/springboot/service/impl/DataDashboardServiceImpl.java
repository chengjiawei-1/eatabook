package com.example.springboot.service.impl;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.example.springboot.entity.CardData;
import com.example.springboot.entity.Dishes;
import com.example.springboot.entity.User;
import com.example.springboot.entity.UserConstitutionTests;
import com.example.springboot.mapper.DataDashboardMapper;
import com.example.springboot.service.DataDashboardService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DataDashboardServiceImpl implements DataDashboardService {

    @Autowired
    private DataDashboardMapper dataDashboardMapper;

    //分词辅助方法
    private static final Set<String> STOP_WORDS = Set.of(
            "的", "了", "和", "是", "在", "怎么", "如何", "什么", "推荐", "做法"
    );

    //使用 HanLP 的中文分词
    private List<String> segmentText(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<String> keywords = new ArrayList<>();
        //分词
        List<Term> terms = HanLP.segment(text);
        for (Term term : terms) {
            String word = term.word;
            //基础过滤
            if (word == null || word.isBlank()) {
                continue;
            }
            //过滤1个字
            if (word.length() < 2) {
                continue;
            }
            //过滤停用词
            if (STOP_WORDS.contains(word)) {
                continue;
            }
            keywords.add(word);
        }
        //去重
        return keywords.stream().distinct().toList();
    }
    //实时分词
    public void processSearchContent(String searchContent) {
        if (searchContent == null || searchContent.isBlank()) {
            return;
        }

        List<String> keywords = segmentText(searchContent);
        LocalDateTime now = LocalDateTime.now();

        for (String keyword : keywords) {
            dataDashboardMapper.upsertKeyword(keyword, now);
        }
    }



    @Override
    public List<CardData> getCardData() {
        List<CardData> cards = new ArrayList<>();

        //添加各类统计数据到卡片中
        cards.add(new CardData("总用户数", "fa-users", getTotalUsers()));
        cards.add(new CardData("菜品数量", "fa-utensils", getAllDishes()));
        cards.add(new CardData("体质测试数", "fa-file-medical", getTotalTests()));
        cards.add(new CardData("古籍数量", "fa-book", getAllBooks()));

        return cards;
    }

    // 获取总用户数
    @Override
    public Integer getTotalUsers() {
        return dataDashboardMapper.findTotalUsers();
    }
    // 获取菜品数量
    @Override
    public Integer getAllDishes() {
        return dataDashboardMapper.findAllDishes();
    }
    // 获取体质测试数量
    @Override
    public Integer getTotalTests() {
        return dataDashboardMapper.findTotalTests();
    }
    // 获取古籍数量
    @Override
    public Integer getAllBooks() {
        return dataDashboardMapper.findAllBooks();
    }

    //环形图-所有用户体质分布
    @Override
    public List<Map<String, Object>> getAllConstitutionDistribution() {
        return dataDashboardMapper.findAllConstitutionDistribution();
    }
    //环形图-指定地区用户体质分布
    @Override
    public List<Map<String, Object>> getConstitutionDistributionByProvince(String province) {
        return dataDashboardMapper.findConstitutionDistributionByProvince(province);
    }

    @Override
    //排行榜
    public List<Map<String, Object>> getTop8Dish(Integer limit) {
        return dataDashboardMapper.findTop8Dish(limit);
    }

    //雷达图-相关体质对应应季菜品
    @Override
    public List<Map<String, Object>> getTop5DishesByConstitutionAndSeason( String season) {
        return dataDashboardMapper.findTop5DishesByConstitutionAndSeason( season);
    }

    // 指定地区地图数据
    @Override
    public List<Map<String, Object>> getMapDataByRegion(String region) {
        return dataDashboardMapper.findMapDataByRegion(region);
    }

    // 获取全国地图数据
    @Override
    public List<Map<String, Object>> getAllMapData() {
        return dataDashboardMapper.findAllMapData();
    }

    //词云图
    //流程调度方法（关键）
    @Override
    public void statTodayHotKeywords() {
        List<String> searchContents = dataDashboardMapper.findTodaySearchContents();
        LocalDateTime now = LocalDateTime.now();

        for (String content : searchContents) {
            List<String> keywords = segmentText(content);
            for (String keyword : keywords) {
                dataDashboardMapper.upsertKeyword(keyword, now);
            }
        }
    }

    //热搜前20关键词
    @Override
    public List<Map<String, Object>> getTop20Keywords() {
        return dataDashboardMapper.findTop20Keywords();
    }








}
