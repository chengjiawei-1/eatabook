package com.example.springboot.service.impl;

import com.example.springboot.entity.PhysiqueAnalysisCache;
import com.example.springboot.mapper.PhysiqueAnalysisCacheMapper;
import com.example.springboot.service.PhysiqueAnalysisCacheService;
import com.example.springboot.utils.AiOutputGuard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class PhysiqueAnalysisCacheServiceImpl implements PhysiqueAnalysisCacheService {

    @Autowired
    private PhysiqueAnalysisCacheMapper analysisMapper;

    @Override
    public String getOrGenerateAnalysis(Integer userId, Date latestTestTime) {
        try {
            // 尝试从数据库获取该用户的缓存记录
            PhysiqueAnalysisCache cache = analysisMapper.selectByUserId(userId);

            if (cache != null && cache.getLastTestTime() != null && latestTestTime != null) {
                long diff = Math.abs(cache.getLastTestTime().getTime() - latestTestTime.getTime());

                if (diff < 2000) {
                    if (AiOutputGuard.isFailureText(cache.getAnalysisContent())) {
                        log.warn("用户 {} 的体质分析缓存是 AI 错误响应，删除后重新生成", userId);
                        analysisMapper.deleteByUserId(userId);
                        return null;
                    }
                    log.info("用户 {} 命中体质分析缓存，时间误差: {}ms", userId, diff);
                    return cache.getAnalysisContent();
                }
                log.info("用户 {} 缓存时间 {} 与最新测试时间 {} 不匹配，准备重新生成",
                        userId, cache.getLastTestTime(), latestTestTime);
            }

            return null;

        } catch (Exception e) {
            log.error("获取体质分析缓存失败: ", e);
            return null;
        }
    }

    @Override
    public int saveOrUpdateAnalysis(PhysiqueAnalysisCache analysis) {
        if (analysis == null || AiOutputGuard.isFailureText(analysis.getAnalysisContent())) {
            log.warn("拒绝保存 AI 错误响应到体质分析缓存");
            return 0;
        }
        return analysisMapper.saveOrUpdateAnalysis(analysis);
    }

    private String callAiToGenerate(Integer userId) {
        return "这是新生成的 AI 分析结果...";
    }

}
