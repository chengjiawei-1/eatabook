package com.example.springboot.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ForumCommentLikeMapper {

    @Select("SELECT COUNT(*) FROM forum_comment_like WHERE comment_id = #{commentId} AND user_id = #{userId}")
    int exists(@Param("commentId") Integer commentId, @Param("userId") Integer userId);

    @Insert("INSERT INTO forum_comment_like(comment_id, user_id) VALUES(#{commentId}, #{userId})")
    int insert(@Param("commentId") Integer commentId, @Param("userId") Integer userId);

    @Delete("DELETE FROM forum_comment_like WHERE comment_id = #{commentId} AND user_id = #{userId}")
    int delete(@Param("commentId") Integer commentId, @Param("userId") Integer userId);
}
