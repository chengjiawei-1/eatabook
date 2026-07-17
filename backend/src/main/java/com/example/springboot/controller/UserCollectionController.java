package com.example.springboot.controller;

import com.example.springboot.entity.UserCollections;
import com.example.springboot.service.UserCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collections")
@CrossOrigin
@Tag(name = "UserCollectionController", description = "用户收藏相关接口")
public class UserCollectionController {

    @Autowired
    private UserCollectionService userCollectionService;

    @Operation(summary = "创建收藏行为")
    @PostMapping
    public Map<String, Object> insert(@RequestBody UserCollections userCollections) {
        if (userCollections.getUserId() == null) {
            return createErrorResponse("用户ID不能为空");
        }
        if (userCollections.getTargetType() == null || userCollections.getTargetType().isEmpty()) {
            return createErrorResponse("收藏类型不能为空");
        }
        if (userCollections.getTargetId() == null) {
            return createErrorResponse("目标ID不能为空");
        }
        if (!"dish".equals(userCollections.getTargetType()) &&
                !"book".equals(userCollections.getTargetType()) &&
                !"ingredient".equals(userCollections.getTargetType())) {
            return createErrorResponse("收藏类型不正确，必须是dish/book/ingredient");
        }
        int result = userCollectionService.insert(userCollections);

        Map<String, Object> res = new HashMap<>();
        if (result > 0) {
            res.put("code", 200);
            res.put("msg", "收藏成功");
            res.put("data", userCollections);
        } else {
            res.put("code", 400);
            res.put("msg", "收藏失败");
        }
        return res;
    }

    @Operation(summary = "取消收藏行为")
    @DeleteMapping("/{collectionId}")
    public Map<String, Object> deleteByUserAndTarget(
            @RequestParam Integer userId,
            @PathVariable Integer collectionId) {

        // 参数验证
        if (userId == null) {
            return createErrorResponse("用户ID不能为空");
        }
        if (collectionId == null) {
            return createErrorResponse("收藏ID不能为空");
        }

        int result = userCollectionService.deleteByUserAndTarget(userId, collectionId);

        Map<String, Object> res = new HashMap<>();
        if (result > 0) {
            res.put("code", 200);
            res.put("msg", "删除收藏成功");
        } else {
            res.put("code", 400);
            res.put("msg", "删除收藏失败");
        }

        return res;
    }


    @Operation(summary = "查询用户所有收藏")
    @GetMapping("/users/{userId}")
    public Map<String, Object> selectByUserId(@PathVariable Integer userId) {
        List<UserCollections> collections = userCollectionService.selectByUserId(userId);

        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", "查询成功");
        res.put("data", collections);
        res.put("count", collections.size());

        return res;
    }

    @Operation(summary = "按类别查询用户收藏")
    @GetMapping("/users/{userId}/types/{targetType}")
    public Map<String, Object> selectByUserAndType(
            @PathVariable Integer userId,
            @PathVariable String targetType) {

        // 验证收藏类型
        if (!"dish".equals(targetType) &&
                !"book".equals(targetType) &&
                !"ingredient".equals(targetType)) {
            return createErrorResponse("收藏类型不正确，必须是dish/book/ingredient");
        }

        List<UserCollections> collections = userCollectionService.selectByUserAndType(userId, targetType);

        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("msg", "查询成功");
        res.put("data", collections);
        res.put("count", collections.size());

        return res;
    }




    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        result.put("msg", message);
        return result;
    }
}
