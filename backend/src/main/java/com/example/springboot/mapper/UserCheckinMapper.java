package com.example.springboot.mapper;

import com.example.springboot.entity.UserCheckin;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserCheckinMapper{

    //插入打卡记录
    @Insert("INSERT INTO user_checkins(user_id, checkin_date, is_rewarded, current_streak, last_checkin_date) " +
            "VALUES(#{userId}, #{checkinDate}, #{isRewarded}, #{currentStreak}, #{lastCheckinDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCheckin(UserCheckin userCheckin);

    //查询用户最新的一条打卡记录
    @Select("SELECT * FROM user_checkins WHERE user_id = #{userId} ORDER BY checkin_date DESC LIMIT 1")
    UserCheckin findLatestByUserId(Integer userId);

    //查询日期范围内的打卡记录
    @Select("SELECT checkin_date FROM user_checkins " +
            "WHERE user_id = #{userId} AND checkin_date BETWEEN #{startDate} AND #{endDate}")
    List<LocalDate> findCheckinDatesInRange(Integer userId,LocalDate startDate,LocalDate endDate);

    //检查用户今天是否已经打过卡
    @Select("SELECT COUNT(*) FROM user_checkins WHERE user_id = #{userId} AND checkin_date = #{today}")
    int countTodayCheckin(Integer userId,LocalDate today);

    //根据ID更新奖励发放状态
    @Update("UPDATE user_checkins SET is_rewarded = #{isRewarded} WHERE id = #{id}")
    int updateRewardStatus(Integer id, Integer isRewarded);

    //查看打卡总数
    @Select("SELECT COUNT(*) as totalCheckins FROM user_checkins")
    Integer totalCheckins();
    //查看今日打卡数
    @Select("SELECT COUNT(*) as todayCheckins FROM user_checkins WHERE DATE(checkin_date) = CURDATE()")
    Integer todayCheckins();
    //查看昨日打卡数
    @Select("SELECT COUNT(*) as yesterdayCheckins FROM user_checkins WHERE DATE(checkin_date) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Integer yesterdayCheckins();
}