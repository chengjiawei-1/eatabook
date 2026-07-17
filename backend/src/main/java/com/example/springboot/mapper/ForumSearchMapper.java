package com.example.springboot.mapper;

import com.example.springboot.entity.ForumsearchRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ForumSearchMapper {
    //插入搜索记录
    @Insert("INSERT INTO forum_search_record(user_id, search_content, search_time, update_time) " +
            "VALUES(#{userId}, #{searchContent}, #{searchTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "searchId")
    int insert(ForumsearchRecord record);

    //查询用户搜索记录
    @Select("SELECT * FROM forum_search_record WHERE user_id = #{userId} ORDER BY search_time DESC LIMIT 10")
    List<ForumsearchRecord> selectByUserId(@Param("userId") Integer userId);

    //删除特定用户的全部搜索记录
    @Delete("DELETE FROM forum_search_record WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Integer userId);

    //删除特定搜索记录
    @Delete("DELETE FROM forum_search_record WHERE search_id = #{searchId}")
    int deleteById(@Param("searchId") Integer searchId);

    //删除特定用户的特定搜索内容记录
    @Delete("DELETE FROM forum_search_record WHERE user_id = #{userId} AND search_content = #{searchContent}")
    int deleteByUserIdAndContent(@Param("userId") Integer userId,
                                 @Param("searchContent") String searchContent);
}
