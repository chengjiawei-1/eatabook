package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot.entity.PhysiqueAnalysisCache;
import com.example.springboot.mapper.PhysiqueAnalysisCacheMapper;
import com.example.springboot.service.PhysiqueAnalysisCacheService;
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
        return analysisMapper.saveOrUpdateAnalysis(analysis);
    }

    private String callAiToGenerate(Integer userId) {
        return "这是新生成的 AI 分析结果...";
    }

}