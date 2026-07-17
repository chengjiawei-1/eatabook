package com.example.springboot.service;

import com.example.springboot.entity.UserBehaviorLog;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface UserBehaviorService {

    //记录用户行为
    void logUserBehavior(UserBehaviorLog behaviorLog);

    //记录点击查看详情行为，返回会话ID
    String logClickView(Long userId, Long dishId, HttpServletRequest request);

    //记录收藏菜品行为，关联之前的点击行为
    void logFavoriteDish(Long userId, Long dishId, String refererSessionId, HttpServletRequest request);

    //获取首页推荐用户行为分析
    Map<String, Object> getHomeRecommendationAnalysis();

    //获取点击转化率分析
    Map<String, Object> getClickConversionAnalysis();

    //获取热门菜品推荐效果
    Map<String, Object> getHotDishRecommendationEffect();

    //获取用户行为小时分布
    Map<String, Object> getUserBehaviorHourlyDistribution();
}