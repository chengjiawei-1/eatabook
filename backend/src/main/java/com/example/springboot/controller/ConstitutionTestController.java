package com.example.springboot.controller;

import com.example.springboot.entity.UserConstitutionTests;
import com.example.springboot.req.ConstitutionTestReq;
import com.example.springboot.res.ConstitutionTestRes;
import com.example.springboot.service.ConstitutionAdviceGenerationService;
import com.example.springboot.service.UserConstitutionTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name="ConstitutionTestController",description="问卷测试相关接口")
@RestController
@RequestMapping("/api/constitution-tests")
@CrossOrigin
public class ConstitutionTestController {

    @Autowired
    private UserConstitutionTestService constitutionService;

    @Autowired
    private ConstitutionAdviceGenerationService adviceGenerationService;

    //提交测试结果
    @Operation(summary = "提交测试结果", description = "提交该用户当前测试结果")
    @PostMapping
    public ConstitutionTestRes submit(@RequestBody ConstitutionTestReq req) {
        return constitutionService.submitTest(req);
    }

    @PostMapping("/advice")
    public ConstitutionAdviceGenerationService.ConstitutionAdviceResult generateAdvice(
            @RequestBody ConstitutionAdviceGenerationService.ConstitutionAdviceRequest req) {
        return adviceGenerationService.generate(req);
    }

    //获取历史记录
    @Operation(summary = "测试结果查询", description = "查询用户所有测试结果")
    @GetMapping("/users/{userId}")
    public List<UserConstitutionTests> getHistory(@PathVariable Integer userId) {
        return constitutionService.getHistory(userId);
    }

    @Operation(summary = "测试分数查询", description = "查询用户测试分数")
    @GetMapping("/users/{userId}/scores")
    public List<UserConstitutionTests> selectByUserIdForArgent(@PathVariable Integer userId) {
        return constitutionService.selectByUserIdForArgent(userId);
    }

    @Operation(summary = "总体质测试次数", description = "总体质测试次数")
    @GetMapping("/count")
    public Integer totalcount() {
        return constitutionService.totalcount();
    }

    @Operation(summary = "今日质测试次数", description = "今日质测试次数")
    @GetMapping("/today/count")
    public Integer todaycount() {
        return constitutionService.todaycount();
    }

    @Operation(summary = "昨日质测试次数", description = "昨日质测试次数")
    @GetMapping("/yesterday/count")
    public Integer yesterdaycount() {
        return constitutionService.yesterdaycount();
    }
}
