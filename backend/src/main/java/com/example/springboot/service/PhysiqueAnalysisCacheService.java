package com.example.springboot.service;

import com.example.springboot.entity.PhysiqueAnalysisCache;

public interface PhysiqueAnalysisCacheService {
    //获取体质深度分析
    String getOrGenerateAnalysis(Integer userId, java.util.Date latestTestTime);
    int saveOrUpdateAnalysis(PhysiqueAnalysisCache analysis);
}