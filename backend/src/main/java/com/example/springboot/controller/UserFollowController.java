package com.example.springboot.controller;

import com.example.springboot.entity.Notification;
import com.example.springboot.service.NotificationService;
import com.example.springboot.service.UserFollowService;
import com.example.springboot.utils.JwtUtils;
import com.example.springboot.utils.NotificationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
@Tag(name = "UserFollowController", description = "用户关注相关接口")
public class UserFollowController {

    @Autowired
    private UserFollowService userFollowService;

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "关注用户")
    @PutMapping("/me/following/{targetUserId}")
    public Map<String, Object> follow(
            @PathVariable Integer targetUserId,
            @RequestHeader("Authorization") String token
    ) {
            Map<String, Object> res = new HashMap<>();
            try {
            Integer userId = JwtUtils.parseToken(token);

            if (targetUserId == null) {
                res.put("code", 400);
                res.put("msg", "参数缺失");
                return res;
            }

            if (userId.equals(targetUserId)) {
                res.put("code", 400);
                res.put("msg", "不能关注自己");
                return res;
            }

            boolean success = userFollowService.follow(userId, targetUserId);
            if (success) {
                Notification notification = NotificationUtils.followNotification(userId, targetUserId);
                notificationService.sendNotification(notification);

                res.put("code", 200);
                res.put("msg", "关注成功");
            } else {
                res.put("code", 500);
                res.put("msg", "关注失败");
            }
            return res;
        } catch (Exception e) {
            res.put("code", 500);
            res.put("msg", e.getMessage());
            return res;
        }
    }

    //取消关注
    @Operation(summary = "取消关注用户")
    @DeleteMapping("/me/following/{targetUserId}")
    public Map<String, Object> unfollow(
            @PathVariable Integer targetUserId,
            @RequestHeader("Authorization") String token
    ) {
            Map<String, Object> res = new HashMap<>();
            try {
            Integer userId = JwtUtils.parseToken(token);

            boolean success = userFollowService.unfollow(userId, targetUserId);
            res.put("code", success ? 200 : 500);
            res.put("msg", success ? "取消关注成功" : "操作失败");
            return res;
        } catch (Exception e) {
            res.put("code", 500);
            res.put("msg", e.getMessage());
            return res;
        }
    }
    //检查是否已关注
    @Operation(summary = "检查是否已关注")
    @GetMapping("/{userId}/following/{targetUserId}")
    public Map<String, Object> isFollowing(@PathVariable Integer userId, @PathVariable Integer targetUserId) {
        Map<String, Object> res = new HashMap<>();
        boolean isFollowing = userFollowService.isFollowing(userId, targetUserId);
        res.put("code", 200);
        res.put("msg", "查询成功");
        res.put("data", isFollowing);
        return res;
    }

    //获取用户的关注数和粉丝数
    @Operation(summary = "获取用户的关注数和粉丝数")
    @GetMapping("/{userId}/stats")
    public Map<String, Object> getUserStats(@PathVariable Integer userId) {
        Map<String, Object> stats = userFollowService.getUserStats(userId);

        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", "获取成功");
        res.put("data", stats);
        return res;
    }

    //获取关注列表
    @Operation(summary = "获取关注列表")
    @GetMapping("/{userId}/following")
    public Map<String, Object> getFollows(@PathVariable Integer userId) {
        List<Map<String, Object>> list = userFollowService.getFollowList(userId, userId);

        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", "获取成功");
        res.put("data", list);
        return res;
    }

    //获取粉丝列表
    @Operation(summary = "获取粉丝列表")
    @GetMapping("/{userId}/followers")
    public Map<String, Object> getFans(@PathVariable Integer userId) {
        List<Map<String, Object>> list = userFollowService.getFanList(userId, userId);

        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", "获取成功");
        res.put("data", list);
        return res;
    }
}