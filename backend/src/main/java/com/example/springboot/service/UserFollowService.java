package com.example.springboot.service;

import java.util.List;
import java.util.Map;

public interface UserFollowService {
    // 关注
    boolean follow(Integer followerId, Integer targetUserId);

    // 取消关注
    boolean unfollow(Integer followerId, Integer targetUserId);

    // 检查是否已关注
    boolean isFollowing(Integer followerId, Integer targetUserId);

    // 获取用户的关注/粉丝统计数据
    Map<String, Object> getUserStats(Integer userId);

    //获取关注列表
    List<Map<String, Object>> getFollowList(Integer userId, Integer currentUserId);

    //获取粉丝列表
    List<Map<String, Object>> getFanList(Integer userId, Integer currentUserId);
}