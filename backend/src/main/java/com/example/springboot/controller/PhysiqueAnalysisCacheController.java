package com.example.springboot.controller;

import com.example.springboot.entity.PhysiqueAnalysisCache;
import com.example.springboot.service.PhysiqueAnalysisCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
@Slf4j
@Tag(name="PhysiqueAnalysisCacheController", description="体质深度分析缓存接口")
@CrossOrigin
@RestController
@RequestMapping("/api/constitution-analysis")
public class PhysiqueAnalysisCacheController {

    @Autowired
    private PhysiqueAnalysisCacheService analysisService;

    //获取深度分析结果
    @Operation(summary = "获取深度分析", description = "校验最新测试时间，命中缓存则直接返回分析文本")

    @GetMapping
    public ResponseEntity<?> getAnalysis(
            @RequestParam("userId") Integer userId,
            @RequestParam("latestTestTime") Long latestTestTime) { // 必须用 Long 接收时间戳数字

        try {
            Date searchDate = new Date(latestTestTime);

            String result = analysisService.getOrGenerateAnalysis(userId, searchDate);

            return ResponseEntity.ok(result != null ? result : "");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("服务器内部错误: " + e.getMessage());
        }
    }

    //保存从AI生成的分析文本
    @Operation(summary = "保存从AI生成的分析文本")
    @PostMapping
    public String saveAnalysis(@RequestBody Map<String, Object> params) {
        log.info("接收到保存分析请求: {}", params);
        try {
            Integer userId = Integer.valueOf(params.get("userId").toString());

            String content = (String) params.get("analysisContent");

            Object tsObj = params.get("latestTestTime");
            if (tsObj == null) {
                log.error("保存失败：latestTestTime 为空");
                return "error";
            }
            Long testTimeTs = Long.valueOf(tsObj.toString());

            PhysiqueAnalysisCache entity = new PhysiqueAnalysisCache();
            entity.setUserId(userId);
            entity.setAnalysisContent(content);
            entity.setLastTestTime(new Date(testTimeTs));
            entity.setTestCount(6);

            int rows = analysisService.saveOrUpdateAnalysis(entity);
            log.info("保存成功，影响行数: {}", rows);
            return "success";
        } catch (Exception e) {
            log.error("保存缓存异常详细信息: ", e);
            return "error";
        }
    }
}
