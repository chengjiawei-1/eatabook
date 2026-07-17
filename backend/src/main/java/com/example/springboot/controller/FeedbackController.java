package com.example.springboot.controller;

import com.example.springboot.entity.Feedback;
import com.example.springboot.req.FeedbackReq;
import com.example.springboot.service.FeedbackService;
import com.example.springboot.utils.JwtUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin
@Tag(name = "FeedbackController", description = "用户反馈相关接口")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    private Map<String, Object> resultSuccess(Object data, String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", msg);
        map.put("data", data);
        return map;
    }

    private Map<String, Object> resultError(String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 500);
        map.put("msg", msg);
        map.put("data", null);
        return map;
    }

    private Integer parseToken(String token) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new Exception("Token不能为空");
        }
        String realToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return JwtUtils.parseToken(realToken);
    }

    @Operation(summary = "创建用户反馈", description = "上传用户反馈信息")
    @PostMapping
    public Map<String, Object> submitFeedback(
            @RequestBody FeedbackReq feedbackReq,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        try {
            Feedback feedback = new Feedback();

            if (token != null && !token.isEmpty()) {
                Integer userId = parseToken(token);
                feedback.setUserId(userId);
            }
            feedback.setContent(feedbackReq.getContent());
            feedback.setSubmissionTime(new Date());

            int rows = feedbackService.insert(feedback);
            return rows > 0 ? resultSuccess(null, "提交成功") : resultError("提交失败");
        } catch (Exception e) {
            return resultError("提交失败: " + e.getMessage());
        }
    }

    //管理员分页查看反馈
    @Operation(summary = "管理员分页查看反馈", description = "分页查看用户反馈信息")
    @GetMapping
    public PageInfo<Feedback> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return feedbackService.findByPage(pageNo, pageSize);
    }
}
