package com.example.springboot.mapper;

import com.example.springboot.entity.ForumPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ForumRecommendMapper {
    //热点推荐
    @Select("""
    <script>
    <![CDATA[
    SELECT *
    FROM forum_post
    WHERE status = 1
    ]]>
    <if test="lastPostId != null">
    <![CDATA[
      AND post_id < #{lastPostId}
    ]]>
    </if>
    <![CDATA[
    ORDER BY
    (
      like_count * 3
    + favorite_count * 4
    + comment_count * 2
    ) / (1 + TIMESTAMPDIFF(DAY, create_time, NOW())) DESC
    LIMIT #{limit}
    ]]>
    </script>
    """)
    List<ForumPost> selectHotPosts(
            @Param("limit") int limit,
            @Param("lastPostId") Integer lastPostId
    );

    //个性化推荐
    @Select("""
    <script>
    <![CDATA[
    SELECT fp.*
    FROM forum_post fp
    WHERE fp.status = 1
    AND fp.category_id IN (
        SELECT p.category_id
        FROM forum_post_like l
        JOIN forum_post p ON l.post_id = p.post_id
        WHERE l.user_id = #{userId}
        GROUP BY p.category_id
    )
    ]]>
    <if test="lastPostId != null">
    <![CDATA[
      AND fp.post_id < #{lastPostId}
    ]]>
    </if>
    <![CDATA[
    ORDER BY
    (
      fp.like_count * 3
    + fp.favorite_count * 4
    + fp.comment_count * 2
    ) / (1 + TIMESTAMPDIFF(DAY, fp.create_time, NOW())) DESC
    LIMIT #{limit}
    ]]>
    </script>
    """)
    List<ForumPost> selectPersonalizedPosts(
            @Param("userId") Integer userId,
            @Param("limit") int limit,
            @Param("lastPostId") Integer lastPostId
    );

    //最新推荐
    @Select("""
    <script>
    <![CDATA[
    SELECT *
    FROM forum_post
    WHERE status = 1
    ]]>
    <if test="lastPostId != null">
    <![CDATA[
      AND (create_time, post_id) <= (
        SELECT create_time, post_id
        FROM forum_post
        WHERE post_id = #{lastPostId}
      )
      AND post_id != #{lastPostId}
    ]]>
    </if>
    <![CDATA[
    ORDER BY create_time DESC, post_id DESC
    LIMIT #{limit}
    ]]>
    </script>
    """)
    List<ForumPost> selectLatestPosts(
            @Param("limit") int limit,
            @Param("lastPostId") Integer lastPostId
    );

    //探索推荐
    @Select("""
    <script>
    <![CDATA[
    SELECT *
    FROM forum_post
    WHERE status = 1
    ]]>
    <if test="lastPostId != null">
    <![CDATA[
      AND post_id < #{lastPostId}
    ]]>
    </if>
    <![CDATA[
    AND like_count < 5
    AND comment_count < 3
    AND favorite_count < 3
    ORDER BY post_id DESC
    LIMIT #{limit}
    ]]>
    </script>
    """)
    List<ForumPost> selectExplorePosts(
            @Param("limit") int limit,
            @Param("lastPostId") Integer lastPostId
    );

    //根据ID列表查帖子
    @Select("""
    <script>
        SELECT *
        FROM forum_post
        WHERE status = 1
        AND post_id IN
        <foreach collection="postIds" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
    </script>
    """)
    List<ForumPost> selectPostsByIds(@Param("postIds") Collection<Integer> postIds);

    @Select("""
    <script>
    SELECT *
    FROM forum_post
    WHERE status = 1
    AND post_id NOT IN 
    <foreach collection="excludedPostIds" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
    ORDER BY post_id ASC
    LIMIT #{limit}
    </script>
    """)
    List<ForumPost> selectRandomPosts(
            @Param("excludedPostIds") List<Integer> excludedPostIds,
            @Param("limit") int limit
    );

    @Select("""
    <script>
    SELECT *
    FROM forum_post
    WHERE status = 1
    
    <if test="excludeIds != null and excludeIds.size() > 0">
      AND post_id NOT IN
      <foreach collection="excludeIds"
               item="id"
               open="("
               separator=","
               close=")">
        #{id}
      </foreach>
    </if>
    
    <if test="lastPostId != null">
      AND post_id &lt; #{lastPostId}
    </if>
    
    ORDER BY create_time DESC
    LIMIT #{limit}
    </script>
    """)
    List<ForumPost> selectRemainingPosts(
            @Param("excludeIds") List<Integer> excludeIds,
            @Param("limit") int limit,
            @Param("lastPostId") Integer lastPostId
    );

    @Select("SELECT post_id FROM forum_post WHERE status = 1 ORDER BY post_id")
    List<Integer> getAllPostIds();



}
