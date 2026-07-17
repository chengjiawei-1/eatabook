package com.example.springboot.controller;

import com.example.springboot.entity.UserPointLog;
import com.example.springboot.service.DishVideoService;
import com.example.springboot.VO.DishVideoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dish-videos")
@CrossOrigin
@Tag(name = "DishVideoController", description = "菜品视频和解锁api")
public class DishVideoController {

    @Autowired
    private DishVideoService dishVideoService;

    //获取某菜品的视频列表
    @Operation(summary = "查询菜品视频列表", description = "查询特定菜品的所有视频信息")
    @GetMapping("/{dishId}")
    public Map<String, Object> getList(@PathVariable Long dishId,
                                       @RequestParam(required = false) Integer userId) {
        Map<String, Object> res = new HashMap<>();
        try {
            List<DishVideoVO> list = dishVideoService.getVideosByDishId(dishId, userId);
            res.put("code", 200);
            res.put("msg", "获取成功");
            res.put("data", list);
        } catch (Exception e) {
            res.put("code", 500);
            res.put("msg", "获取失败：" + e.getMessage());
        }
        return res;
    }

    //解锁视频
    @Operation(summary = "解锁视频", description = "解锁视频信息")
    @PostMapping("/unlocks")
    public Map<String, Object> unlock(@RequestBody Map<String, Object> params) {
        Map<String, Object> res = new HashMap<>();
        try {
            Integer userId = (Integer) params.get("userId");

            Long videoId = Long.valueOf(params.get("videoId").toString());

            if (userId == null || videoId == null) {
                res.put("code", 400);
                res.put("msg", "参数错误");
                return res;
            }

            boolean success = dishVideoService.unlockVideo(userId, videoId);

            res.put("code", 200);
            res.put("msg", "解锁成功");
        } catch (RuntimeException e) {

            res.put("code", 400);
            res.put("msg", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            res.put("code", 500);
            res.put("msg", "系统异常");
        }
        return res;
    }

    @Operation(summary = "新增积分流水记录")
    @PostMapping("/point-logs")
    public int insertPointLog(@RequestBody UserPointLog log) {
        return dishVideoService.insertPointLog(log);
    }
}