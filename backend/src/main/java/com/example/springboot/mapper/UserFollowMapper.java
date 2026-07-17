package com.example.springboot.mapper;

import com.example.springboot.entity.UserFollow;
import org.apache.ibatis.annotations.*;
        import java.util.List;
import java.util.Map;

@Mapper
public interface UserFollowMapper {

    //插入关注记录
    @Insert("INSERT INTO user_follows(follower_id, following_id, create_time) VALUES(#{followerId}, #{followingId}, NOW())")
    int insert(UserFollow userFollow);

    //删除关注记录
    @Delete("DELETE FROM user_follows WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    int delete(@Param("followerId") Integer followerId, @Param("followingId") Integer followingId);

    //查询是否存在关注关系
    @Select("SELECT COUNT(*) FROM user_follows WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    int countByFollowerAndFollowing(@Param("followerId") Integer followerId, @Param("followingId") Integer followingId);

    //获取某人的粉丝列表ID
    @Select("SELECT follower_id FROM user_follows WHERE following_id = #{userId}")
    List<Integer> selectFollowersByUserId(Integer userId);

    //获取某人的关注列表ID
    @Select("SELECT following_id FROM user_follows WHERE follower_id = #{userId}")
    List<Integer> selectFollowingByUserId(Integer userId);

    //获取我的关注列表
    @Select("SELECT u.user_id as userId, u.username, u.avatar_url as avatarUrl, u.email " +
            "FROM user_follows f " +
            "LEFT JOIN users u ON f.following_id = u.user_id " +
            "WHERE f.follower_id = #{userId}")
    List<Map<String, Object>> getFollowList(@Param("userId") Integer userId);

    //获取我的粉丝列表
    @Select("SELECT u.user_id as userId, u.username, u.avatar_url as avatarUrl, u.email " +
            "FROM user_follows f " +
            "LEFT JOIN users u ON f.follower_id = u.user_id " +
            "WHERE f.following_id = #{userId}")
    List<Map<String, Object>> getFanList(@Param("userId") Integer userId);
}