package com.example.springboot.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishRecommendMapper {
    //查询用户最后一次体质测试得分最高的体质
    @Select("""
        SELECT main_constitution
        FROM user_constitution_tests
        WHERE user_id = #{userId}
        ORDER BY test_time DESC
        LIMIT 1
    """)
    String findLastTestMainConstitution(@Param("userId") Integer userId);

    // 查询用户收藏的菜品标签
    @Select("""
        SELECT d.effect_tag1, d.effect_tag2, d.effect_tag3, d.effect_tag4, d.effect_tag5
        FROM dishes d
        INNER JOIN user_collections uc ON d.dish_id = uc.target_id
        WHERE uc.user_id = #{userId} AND uc.target_type = 'dish'
    """)
    List<Map<String, Object>> findUserFavoriteTags(@Param("userId") Integer userId);

    // 查询所有菜品及其相关信息
    @Select("""
        SELECT 
            d.dish_id,
            d.dish_name,
            d.kind,
            d.dish_img_url,
            d.effect_tag1, d.effect_tag2, d.effect_tag3, d.effect_tag4, d.effect_tag5,
            d.season_spring, d.season_summer, d.season_autumn, d.season_winter,
            d.suitable_constitution,
            d.unsuitable_constitution
        FROM dishes d
    """)
    List<Map<String, Object>> findAllDishesWithDetails();

    // 查询用户所在地区
    @Select("""
        SELECT native_place, city
        FROM users
        WHERE user_id = #{userId}
    """)
    Map<String, Object> findUserLocation(@Param("userId") Integer userId);
}
