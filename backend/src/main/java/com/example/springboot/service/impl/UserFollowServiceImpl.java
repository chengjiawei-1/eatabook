package com.example.springboot.service.impl;

import com.example.springboot.entity.User;
import com.example.springboot.entity.UserFollow;
import com.example.springboot.mapper.UserFollowMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.UserFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserFollowServiceImpl implements UserFollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public boolean follow(Integer followerId, Integer targetUserId) {
        // 不能关注自己
        if (followerId.equals(targetUserId)) {
            throw new RuntimeException("不能关注自己");
        }

        // 检查是否已经关注
        int count = userFollowMapper.countByFollowerAndFollowing(followerId, targetUserId);
        if (count > 0) {
            return true;
        }

        // 插入关注记录
        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(targetUserId);
        int rows = userFollowMapper.insert(follow);

        if (rows > 0) {

            userMapper.updateFollowingCount(followerId, 1);

            userMapper.updateFollowerCount(targetUserId, 1);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean unfollow(Integer followerId, Integer targetUserId) {
        // 删除关注记录
        int rows = userFollowMapper.delete(followerId, targetUserId);

        if (rows > 0) {

            userMapper.updateFollowingCount(followerId, -1);
            userMapper.updateFollowerCount(targetUserId, -1);
            return true;
        }
        return false;
    }

    @Override
    public boolean isFollowing(Integer followerId, Integer targetUserId) {
        return userFollowMapper.countByFollowerAndFollowing(followerId, targetUserId) > 0;
    }

    @Override
    public Map<String, Object> getUserStats(Integer userId) {
        User user = userMapper.selectById(userId);
        Map<String, Object> map = new HashMap<>();
        if (user != null) {
            map.put("followingCount", user.getFollowingCount());
            map.put("followerCount", user.getFollowerCount());
        } else {
            map.put("followingCount", 0);
            map.put("followerCount", 0);
        }
        return map;
    }

    @Override
    public List<Map<String, Object>> getFollowList(Integer userId, Integer currentUserId) {
        List<Map<String, Object>> list = userFollowMapper.getFollowList(userId);
        if (currentUserId != null) {
            for (Map<String, Object> user : list) {
                Integer targetId = (Integer) user.get("userId");
                boolean isFollow = isFollowing(currentUserId, targetId);
                user.put("isFollowing", isFollow);
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getFanList(Integer userId, Integer currentUserId) {
        List<Map<String, Object>> list = userFollowMapper.getFanList(userId);
        if (currentUserId != null) {
            for (Map<String, Object> user : list) {
                Integer targetId = (Integer) user.get("userId");
                boolean isFollow = isFollowing(currentUserId, targetId);
                user.put("isFollowing", isFollow);
            }
        }
        return list;
    }
}