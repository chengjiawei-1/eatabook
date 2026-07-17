package com.example.springboot.controller;

import com.example.springboot.entity.UserCheckin;
import com.example.springboot.service.UserCheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name="UserCheckinController", description="用户打卡基础接口")
@CrossOrigin
@RestController
@RequestMapping("/api/checkins")
public class UserCheckinController {

    @Autowired
    private UserCheckinService userCheckinService;

    //打卡记录
    @Operation(summary = "插入记录")
    @PostMapping
    public int insertCheckin(@RequestBody UserCheckin userCheckin){
        return userCheckinService.insertCheckin(userCheckin);
    }

    //获取最新一条
    @Operation(summary = "获取最新")
    @GetMapping("/users/{userId}/latest")
    public UserCheckin findLatestByUserId(@PathVariable Integer userId){
        return userCheckinService.findLatestByUserId(userId);
    }

    //查询范围
    @Operation(summary = "查询范围")
    @GetMapping
    public List<LocalDate> findCheckinDatesInRange(
            @RequestParam Integer userId,
            @RequestParam String startDate,
            @RequestParam String endDate){
        return userCheckinService.findCheckinDatesInRange(userId, LocalDate.parse(startDate), LocalDate.parse(endDate));
    }

    //检查今天是否打过
    @Operation(summary = "今日计数")
    @GetMapping("/today/count")
    public int countTodayCheckin(
            @RequestParam Integer userId,
            @RequestParam String today){
        return userCheckinService.countTodayCheckin(userId, LocalDate.parse(today));
    }

    @Operation(summary = "打卡总数")
    @GetMapping("/count")
    public Integer totalCheckins(){
        return userCheckinService.totalCheckins();
    }
    @Operation(summary = "今日打卡数")
    @GetMapping("/today/total")
    public Integer todayCheckins(){
        return userCheckinService.todayCheckins();
    }
    @Operation(summary = "昨日打卡数")
    @GetMapping("/yesterday/total")
    public Integer yesterdayCheckins(){
        return userCheckinService.yesterdayCheckins();
    }
}
