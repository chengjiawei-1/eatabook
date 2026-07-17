package com.example.springboot.mapper;

import com.example.springboot.entity.UserMealPlan;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMealPlanMapper {

    //根据用户ID查询现有的饮食计划
    @Select("SELECT * FROM user_meal_plan WHERE user_id = #{userId}")
    UserMealPlan selectByUserId(@Param("userId") Integer userId);

    //插入或更新用户饮食计划
    @Insert("INSERT INTO user_meal_plan(user_id, plan_json, generate_date, base_constitution, update_time) " +
            "VALUES(#{userId}, #{planJson}, #{generateDate}, #{baseConstitution}, NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "plan_json = VALUES(plan_json), " +
            "generate_date = VALUES(generate_date), " +
            "base_constitution = VALUES(base_constitution), " +
            "update_time = NOW()")
    int saveOrUpdatePlan(UserMealPlan plan);

    //删除某个用户的计划
    @Delete("DELETE FROM user_meal_plan WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Integer userId);
}