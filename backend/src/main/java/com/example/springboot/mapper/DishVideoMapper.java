package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.DishVideo;
import com.example.springboot.entity.UserPointLog;
import com.example.springboot.entity.UserVideoUnlock;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface DishVideoMapper {
    // 查询某菜品的所有视频
    @Select("SELECT * FROM dish_video WHERE dish_id = #{dishId}")
    List<DishVideo> selectByDishId(Long dishId);

    // 根据ID查询单个视频
    @Select("SELECT * FROM dish_video WHERE id = #{id}")
    DishVideo selectById(Long id);

    // 查询用户是否解锁了某个视频
    @Select("SELECT COUNT(*) FROM user_video_unlock WHERE user_id = #{userId} AND video_id = #{videoId}")
    int countUnlock(@Param("userId") Integer userId, @Param("videoId") Long videoId);

    // 插入解锁记录
    @Insert("INSERT INTO user_video_unlock(user_id, video_id, unlock_time) VALUES(#{userId}, #{videoId}, #{unlockTime})")
    int insertUnlock(UserVideoUnlock unlock);

    // 插入积分流水
    @Insert("INSERT INTO user_point_log(user_id, type, amount, balance, description, create_time) " +
            "VALUES(#{userId}, #{type}, #{amount}, #{balance}, #{description}, #{createTime})")
    int insertPointLog(UserPointLog log);
    }