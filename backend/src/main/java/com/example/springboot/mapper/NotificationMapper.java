package com.example.springboot.mapper;

import com.example.springboot.entity.Notification;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NotificationMapper {

    @Insert("INSERT INTO notifications(user_id, type, title, content, link, related_id, read_status, created_at) " +
            "VALUES(#{userId}, #{type}, #{title}, #{content}, #{link}, #{relatedId}, #{readStatus}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);

    @Select("SELECT * FROM notifications WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<Notification> findByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT * FROM notifications WHERE id = #{id} AND user_id = #{userId}")
    Notification findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE notifications SET read_status = 1, read_at = NOW() WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);

    @Update("UPDATE notifications SET read_status = 1, read_at = NOW() WHERE user_id = #{userId} AND read_status = 0")
    int markAllAsRead(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} AND read_status = 0")
    int countUnread(@Param("userId") Long userId);

    @Delete("DELETE FROM notifications WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}