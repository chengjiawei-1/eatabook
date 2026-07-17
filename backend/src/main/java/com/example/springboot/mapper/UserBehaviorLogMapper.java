package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.UserBehaviorLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserBehaviorLogMapper extends BaseMapper<UserBehaviorLog> {

    //获取首页推荐点击和收藏的对比数据
    @Select("SELECT " +
            "    DATE(create_time) as date, " +
            "    behavior_type, " +
            "    COUNT(DISTINCT user_id) as user_count, " +
            "    COUNT(DISTINCT session_id) as session_count, " +
            "    COUNT(*) as total_count " +
            "FROM user_behavior_log " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "AND behavior_type IN ('CLICK_VIEW', 'FAVORITE_DISH') " +
            "GROUP BY DATE(create_time), behavior_type " +
            "ORDER BY date ASC")
    List<Map<String, Object>> getRecommendationBehaviorTrend();

    //获取点击转化率分析
    @Select("SELECT " +
            "    DATE(click.create_time) as date, " +
            "    COUNT(DISTINCT click.session_id) as click_sessions, " +
            "    COUNT(DISTINCT favorite.session_id) as favorite_sessions, " +
            "    ROUND(COUNT(DISTINCT favorite.session_id) * 100.0 / COUNT(DISTINCT click.session_id), 2) as conversion_rate " +
            "FROM user_behavior_log click " +
            "LEFT JOIN user_behavior_log favorite ON click.session_id = favorite.referer_session_id " +
            "    AND favorite.behavior_type = 'FAVORITE_DISH' " +
            "    AND favorite.create_time >= click.create_time " +
            "    AND DATE(favorite.create_time) = DATE(click.create_time) " +
            "WHERE click.behavior_type = 'CLICK_VIEW' " +
            "    AND click.create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(click.create_time) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> getClickToFavoriteConversion();

    //获取热门菜品推荐效果
    @Select("SELECT " +
            "    dish_id, " +
            "    COUNT(DISTINCT CASE WHEN behavior_type = 'CLICK_VIEW' THEN session_id END) as click_sessions, " +
            "    COUNT(DISTINCT CASE WHEN behavior_type = 'FAVORITE_DISH' THEN session_id END) as favorite_sessions, " +
            "    ROUND(COUNT(DISTINCT CASE WHEN behavior_type = 'FAVORITE_DISH' THEN session_id END) * 100.0 / " +
            "          COUNT(DISTINCT CASE WHEN behavior_type = 'CLICK_VIEW' THEN session_id END), 2) as conversion_rate " +
            "FROM user_behavior_log " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "AND behavior_type IN ('CLICK_VIEW', 'FAVORITE_DISH') " +
            "GROUP BY dish_id " +
            "HAVING click_sessions > 0 " +
            "ORDER BY click_sessions DESC, conversion_rate DESC " +
            "LIMIT 10")
    List<Map<String, Object>> getHotDishRecommendationEffect();

    //获取用户行为统计
    @Select("SELECT " +
            "    HOUR(create_time) as hour, " +
            "    behavior_type, " +
            "    COUNT(*) as count " +
            "FROM user_behavior_log " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 1 DAY) " +
            "AND behavior_type IN ('CLICK_VIEW', 'FAVORITE_DISH') " +
            "GROUP BY HOUR(create_time), behavior_type " +
            "ORDER BY hour ASC")
    List<Map<String, Object>> getUserBehaviorHourlyDistribution();
}