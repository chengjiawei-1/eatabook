package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.UserBehaviorLog;
import com.example.springboot.mapper.UserBehaviorLogMapper;
import com.example.springboot.service.UserBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserBehaviorServiceImpl
        extends ServiceImpl<UserBehaviorLogMapper, UserBehaviorLog>
        implements UserBehaviorService {

    @Autowired
    private UserBehaviorLogMapper userBehaviorLogMapper;

    //记录用户行为
    @Override
    public void logUserBehavior(UserBehaviorLog behaviorLog) {
        this.save(behaviorLog);
    }

    //记录点击查看详情行为
    @Override
    public String logClickView(Long userId, Long dishId, HttpServletRequest request) {
        try {
            String sessionId = UUID.randomUUID().toString();
            UserBehaviorLog log = new UserBehaviorLog();

            log.setUserId(userId);
            log.setDishId(dishId);
            log.setBehaviorType("CLICK_VIEW");
            log.setSessionId(sessionId);
            log.setIpAddress(request.getRemoteAddr());
            String ua = request.getHeader("User-Agent");
            if (ua != null && ua.length() > 250) {
                ua = ua.substring(0, 250);
            }
            log.setUserAgent(ua);
            log.setCreateTime(LocalDateTime.now());
            log.setUpdateTime(LocalDateTime.now());

            System.out.println("准备插入数据库: " + log);
            this.save(log);
            return sessionId;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("数据库写入失败：" + e.getMessage());
        }
    }

    //记录收藏行为
    @Override
    public void logFavoriteDish(Long userId, Long dishId,
                                String refererSessionId,
                                HttpServletRequest request) {

        UserBehaviorLog log = new UserBehaviorLog();
        log.setUserId(userId);
        log.setDishId(dishId);
        log.setBehaviorType("FAVORITE_DISH");
        log.setSessionId(UUID.randomUUID().toString());
        log.setRefererSessionId(refererSessionId);
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setCreateTime(LocalDateTime.now());
        log.setUpdateTime(LocalDateTime.now());

        this.save(log);
    }

    //首页推荐分析
    @Override
    public Map<String, Object> getHomeRecommendationAnalysis() {
        List<Map<String, Object>> rawData = userBehaviorLogMapper.getRecommendationBehaviorTrend();

        List<String> dates = new ArrayList<>();
        List<Long> clickData = new ArrayList<>();
        List<Long> favoriteData = new ArrayList<>();

        Map<String, Long> clickMap = new HashMap<>();
        Map<String, Long> favMap = new HashMap<>();

        for (Map<String, Object> row : rawData) {
            String date = row.get("date").toString();
            String type = row.get("behavior_type").toString();
            Long count = ((Number) row.get("total_count")).longValue();

            if ("CLICK_VIEW".equals(type)) clickMap.put(date, count);
            if ("FAVORITE_DISH".equals(type)) favMap.put(date, count);
        }

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        java.time.LocalDate today = java.time.LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            String dateStr = today.minusDays(i).format(formatter);
            dates.add(dateStr);
            clickData.add(clickMap.getOrDefault(dateStr, 0L));
            favoriteData.add(favMap.getOrDefault(dateStr, 0L));
        }

        long totalClicks = clickData.stream().mapToLong(Long::longValue).sum();
        long totalFavs = favoriteData.stream().mapToLong(Long::longValue).sum();
        double rate = totalClicks == 0 ? 0 : (double) totalFavs / totalClicks * 100;

        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("clickData", clickData);
        result.put("favoriteData", favoriteData);
        result.put("totalClicks", totalClicks);
        result.put("totalFavorites", totalFavs);
        result.put("conversionRate", Math.round(rate * 100.0) / 100.0);

        return result;
    }

    //点击-收藏 转化率分析

    @Override
    public Map<String, Object> getClickConversionAnalysis() {

        List<Map<String, Object>> data =
                userBehaviorLogMapper.getClickToFavoriteConversion();

        Map<String, Object> result = new HashMap<>();
        result.put("conversion", data);

        return result;
    }

    //热门菜品推荐效果
    @Override
    public Map<String, Object> getHotDishRecommendationEffect() {

        List<Map<String, Object>> data =
                userBehaviorLogMapper.getHotDishRecommendationEffect();

        Map<String, Object> result = new HashMap<>();
        result.put("hotDishEffect", data);

        return result;
    }

    //用户行为小时分布
    @Override
    public Map<String, Object> getUserBehaviorHourlyDistribution() {

        List<Map<String, Object>> data =
                userBehaviorLogMapper.getUserBehaviorHourlyDistribution();

        Map<String, Object> result = new HashMap<>();
        result.put("hourlyDistribution", data);

        return result;
    }
}
