package com.example.springboot.mapper;

import com.example.springboot.entity.ForumComment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ForumCommentMapper {

    @Select("SELECT * FROM forum_comment WHERE comment_id = #{commentId}")
    ForumComment selectById(Integer commentId);

    @Insert("INSERT INTO forum_comment(post_id, user_id, content, parent_id, reply_user_id, " +
            "like_count, status, create_time, update_time) " +
            "VALUES(#{postId}, #{userId}, #{content}, #{parentId}, #{replyUserId}, 0, 1, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "commentId")
    int insert(ForumComment comment);
    //按照帖子ID查询评论
    @Select("SELECT * FROM forum_comment WHERE post_id = #{postId} AND status = 1 ORDER BY create_time ASC")
    List<ForumComment> selectByPostId(Integer postId);

    @Update("UPDATE forum_comment SET like_count = like_count + #{delta} WHERE comment_id = #{commentId}")
    int incLike(@Param("commentId") Integer commentId, @Param("delta") int delta);

    @Delete("DELETE FROM forum_comment WHERE comment_id = #{commentId}")
    int deleteComment(Integer commentId);
}