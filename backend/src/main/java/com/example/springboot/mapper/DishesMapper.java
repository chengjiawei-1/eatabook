package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Dishes;
import org.apache.ibatis.annotations.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Mapper
public interface DishesMapper extends BaseMapper<Dishes> {

    @Select("SELECT count(*) FROM dishes WHERE dish_name = #{dishName}")
    int countByName(@Param("dishName") String dishName);

    @Delete("DELETE FROM dishes WHERE dish_id = #{dish_id}")
    int deleteDishes(@Param("dish_id") Integer dish_id);

    @Update("UPDATE dishes SET dish_name = #{dishes.dishName}, kind = #{dishes.kind}, effect_tag1 = #{dishes.effectTag1}, effect_tag2 = #{dishes.effectTag2}, effect_tag3 = #{dishes.effectTag3}, effect_tag4 = #{dishes.effectTag4}, effect_tag5 = #{dishes.effectTag5}, season_spring = #{dishes.seasonSpring}, season_summer = #{dishes.seasonSummer}, season_autumn = #{dishes.seasonAutumn}, season_winter = #{dishes.seasonWinter} WHERE dish_id = #{dish_id}")
    int updateDishes(@PathVariable Integer dish_id, @RequestBody Dishes dishes);

    @Select("select * from dishes")
    List<Dishes> findAllDishes();

    @Select("""
        SELECT d.* FROM dishes d
        INNER JOIN dish_book_rel dbr ON d.dish_id = dbr.dish_id
        WHERE dbr.chapter_id=#{chapter_id}
        ORDER BY d.dish_name
    """)
    List<Dishes> findDishesByChapterId(Integer chapter_id);

    @Select("select * from dishes where dish_id=#{dish_id}")
    Dishes findDishesByDishesId(Integer dish_id);

    @Select("""
        <script>
        SELECT * FROM dishes
        <where>
        <if test="kind != null and kind != ''"> AND kind = #{kind} </if>
        <if test="season != null and season != ''">
            <choose>
                <when test="season == '春季'"> AND season_spring = 1 </when>
                <when test="season == '夏季'"> AND season_summer = 1 </when>
                <when test="season == '秋季'"> AND season_autumn = 1 </when>
                <when test="season == '冬季'"> AND season_winter = 1 </when>
                <otherwise> AND 1 = 0 </otherwise>
            </choose>
        </if>
        </where> ORDER BY dish_name
        </script>
    """)
    List<Dishes> filterDishes(@Param("kind") String kind, @Param("season") String season);

    @Select("""
        <script>
        SELECT * FROM dishes
        <where>
            <if test="keyword != null and keyword != ''">
              AND (dish_name LIKE CONCAT('%', #{keyword}, '%')
                  OR effect_tag1 = #{keyword} OR effect_tag2 = #{keyword}
                  OR effect_tag3 = #{keyword} OR effect_tag4 = #{keyword} OR effect_tag5 = #{keyword})
            </if>
            <if test="kind != null and kind != ''"> AND kind = #{kind} </if>
            <if test="season != null and season != ''">
                <choose>
                    <when test="season == '春季'">AND season_spring = 1</when>
                    <when test="season == '夏季'">AND season_summer = 1</when>
                    <when test="season == '秋季'">AND season_autumn = 1</when>
                    <when test="season == '冬季'">AND season_winter = 1</when>
                    <otherwise>AND 1 = 0</otherwise>
                </choose>
            </if>
        </where> ORDER BY dish_name
        </script>
    """)
    List<Dishes> searchDishes(@Param("keyword") String keyword, @Param("kind") String kind, @Param("season") String season);



}
