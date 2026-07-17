package com.example.springboot.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ForumPostFavoriteMapper {
    //检查用户是否已收藏帖子
    @Select("SELECT COUNT(*) FROM forum_post_favorite WHERE post_id = #{postId} AND user_id = #{userId}")
    int exists(@Param("postId") Integer postId, @Param("userId") Integer userId);
    //添加收藏
    @Insert("INSERT IGNORE INTO forum_post_favorite(post_id, user_id, create_time) VALUES(#{postId}, #{userId}, NOW())")
    int insert(@Param("postId") Integer postId, @Param("userId") Integer userId);
    //取消收藏
    @Delete("DELETE FROM forum_post_favorite WHERE post_id = #{postId} AND user_id = #{userId}")
    int delete(@Param("postId") Integer postId, @Param("userId") Integer userId);
    //获取用户收藏的帖子ID列表
    @Select("SELECT post_id FROM forum_post_favorite WHERE user_id = #{userId}")
    List<Integer> selectFavoritePostIdsByUserId(@Param("userId") Integer userId);
    //统计帖子的收藏数
    @Select("SELECT COUNT(*) FROM forum_post_favorite WHERE post_id = #{postId}")
    int countByPostId(@Param("postId") Integer postId);
    //删除用户的所有收藏记录
    @Delete("DELETE FROM forum_post_favorite WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Integer userId);
    //删除帖子的所有收藏记录
    @Delete("DELETE FROM forum_post_favorite WHERE post_id = #{postId}")
    int deleteByPostId(@Param("postId") Integer postId);
}