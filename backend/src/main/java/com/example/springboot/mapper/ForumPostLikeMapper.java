package com.example.springboot.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ForumPostLikeMapper {

    @Select("SELECT COUNT(*) FROM forum_post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    int exists(@Param("postId") Integer postId, @Param("userId") Integer userId);

    @Insert("INSERT IGNORE INTO forum_post_like(post_id, user_id) VALUES(#{postId}, #{userId})")
    int insert(@Param("postId") Integer postId, @Param("userId") Integer userId);

    @Delete("DELETE FROM forum_post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    int delete(@Param("postId") Integer postId, @Param("userId") Integer userId);
}
