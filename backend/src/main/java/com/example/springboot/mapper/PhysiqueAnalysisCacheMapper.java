package com.example.springboot.mapper;

import com.example.springboot.entity.PhysiqueAnalysisCache;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PhysiqueAnalysisCacheMapper {

    //根据用户ID查询最新的体质分析缓存
    @Select("SELECT * FROM physique_analysis_cache WHERE user_id = #{userId}")
    PhysiqueAnalysisCache selectByUserId(@Param("userId") Integer userId);

    //插入或更新用户体质分析缓存
    @Insert("INSERT INTO physique_analysis_cache(user_id, analysis_content, last_test_time, test_count, created_at, updated_at) " +
            "VALUES(#{userId}, #{analysisContent}, #{lastTestTime}, #{testCount}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "analysis_content = #{analysisContent}, " +
            "last_test_time = #{lastTestTime}, " +
            "test_count = #{testCount}, " +
            "updated_at = NOW()")
    int saveOrUpdateAnalysis(PhysiqueAnalysisCache analysis);

    //删除某个用户的分析缓存
    @Delete("DELETE FROM physique_analysis_cache WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Integer userId);
}