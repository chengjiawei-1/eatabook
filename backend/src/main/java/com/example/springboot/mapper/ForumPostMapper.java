package com.example.springboot.mapper;

import com.example.springboot.entity.ForumPost;
import com.example.springboot.entity.ForumPostFavorite;
import com.example.springboot.entity.PostImage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ForumPostMapper {

    //查询帖子列表
    @Select("<script>" +
            "SELECT p.*, u.username, u.avatar_url AS avatarUrl FROM forum_post p " +
            "LEFT JOIN users u ON p.user_id = u.user_id " +
            "WHERE 1=1 " +
            "<if test='status != null'>AND p.status = #{status}</if> " +
            "<if test='categoryId != null'>AND p.category_id = #{categoryId}</if> " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "   AND (p.title LIKE CONCAT('%', #{keyword}, '%') OR p.content LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if> " +
            "ORDER BY p.create_time DESC" +
            "</script>")
    List<ForumPost> selectPost(@Param("categoryId") Integer categoryId,
                               @Param("keyword") String keyword,
                               @Param("status") Integer status);

    //根据帖子ID查询帖子详情
    @Select("SELECT p.*, u.username, u.avatar_url AS avatarUrl FROM forum_post p " +
            "LEFT JOIN users u ON p.user_id = u.user_id " +
            "WHERE p.post_id = #{postId}")
    ForumPost selectById(@Param("postId") Integer postId);

    @Select("SELECT p.*, u.username, u.avatar_url AS avatarUrl, " +
            "(SELECT COUNT(*) FROM forum_post_like WHERE post_id = p.post_id AND user_id = #{currentUserId}) > 0 AS liked, " +
            "(SELECT COUNT(*) FROM forum_post_favorite WHERE post_id = p.post_id AND user_id = #{currentUserId}) > 0 AS favorited " +
            "FROM forum_post p " +
            "LEFT JOIN users u ON p.user_id = u.user_id " +
            "WHERE p.post_id = #{postId}")
    ForumPost selectByIdWithStatus(@Param("postId") Integer postId, @Param("currentUserId") Integer currentUserId);

    //插入新帖子
    @Insert("INSERT INTO forum_post(title, content, category_id, user_id, related_type, related_id, " +
            "cover_image, like_count, comment_count, favorite_count, status, create_time, update_time) " +
            "VALUES(#{title}, #{content}, #{categoryId}, #{userId}, #{relatedType}, #{relatedId}, " +
            "#{coverImage}, #{likeCount}, #{commentCount}, #{favoriteCount}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "postId", keyColumn = "post_id")
    int insert(ForumPost post);

    //更新帖子封面图
    @Update("UPDATE forum_post SET cover_image = #{coverImageUrl} WHERE post_id = #{postId}")
    int updateCoverImage(@Param("postId") Integer postId, @Param("coverImageUrl") String coverImageUrl);

    @Update("UPDATE forum_post SET like_count = GREATEST(0, like_count + #{step}) WHERE post_id = #{postId}")
    int incLike(@Param("postId") Integer postId, @Param("step") Integer step);

    @Update("UPDATE forum_post SET favorite_count = GREATEST(0, favorite_count + #{step}) WHERE post_id = #{postId}")
    int incFavorite(@Param("postId") Integer postId, @Param("step") Integer step);

    //更新帖子评论数
    @Update("UPDATE forum_post SET comment_count = comment_count + #{step} WHERE post_id = #{postId}")
    int incComment(@Param("postId") Integer postId, @Param("step") Integer step);

    //批量插入图片
    @Insert("<script>"+
            "INSERT INTO forum_post_image (post_id, image_url, sort_order, image_type, status, create_time) "+
            "VALUES "+
            "<foreach collection='images' item='image' separator=','>"+
            "(#{image.postId}, #{image.imageUrl}, #{image.sortOrder}, #{image.imageType}, #{image.status}, NOW())"+
            "</foreach>"+
            "</script>")
    int batchInsertImages(@Param("images") List<PostImage> images);

    //删除帖子图片
    @Delete("DELETE FROM forum_post_image WHERE image_id = #{imageId}")
    void deleteImage(@Param("imageId") Integer imageId);

    //根据id查询所有图片
    @Select("SELECT * FROM forum_post_image WHERE post_id = #{postId} AND status = 1 ORDER BY sort_order ASC")
    List<PostImage> selectImagesByPostId(@Param("postId") Integer postId);

    //分页查询帖子评论
    @Select("SELECT c.*, u.username, u.avatar_url AS avatarUrl FROM forum_comment c " +
            "LEFT JOIN users u ON c.user_id = u.user_id " +
            "WHERE c.post_id = #{postId} AND c.status = 1 " +
            "ORDER BY c.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<com.example.springboot.entity.ForumComment> selectCommentsByPostIdWithPaging(
            @Param("postId") Integer postId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize);

    //删除帖子
    @Delete("DELETE FROM forum_comment_like WHERE comment_id IN (SELECT comment_id FROM forum_comment WHERE post_id = #{postId})")
    int deleteCommentLikesByPostId(@Param("postId") Integer postId);

    @Delete("DELETE FROM forum_comment WHERE post_id = #{postId}")
    int deleteCommentsByPostId(@Param("postId") Integer postId);

    @Delete("DELETE FROM forum_post_like WHERE post_id = #{postId}")
    int deletePostLikesByPostId(@Param("postId") Integer postId);

    @Delete("DELETE FROM forum_post_favorite WHERE post_id = #{postId}")
    int deletePostFavoritesByPostId(@Param("postId") Integer postId);

    @Delete("DELETE FROM forum_post_image WHERE post_id = #{postId}")
    int deleteImagesByPostId(@Param("postId") Integer postId);

    @Delete("DELETE FROM forum_post WHERE post_id = #{postId}")
    int deletePost(@Param("postId") Integer postId);

    //查询用户帖子
    @Select("<script>" +
            "SELECT p.*, u.username, u.avatar_url AS avatarUrl FROM forum_post p " +
            "LEFT JOIN users u ON p.user_id = u.user_id " +
            "WHERE p.user_id = #{userId} " +
            "<if test='status != null'>AND p.status = #{status}</if> " +
            "ORDER BY p.create_time DESC" +
            "</script>")
    List<ForumPost> selectUserPosts(@Param("userId") Integer userId, @Param("status") Integer status);

    //查询点赞量前3的帖子
    @Select("SELECT p.*, u.username, u.avatar_url AS avatarUrl FROM forum_post p " +
            "LEFT JOIN users u ON p.user_id = u.user_id " +
            "WHERE p.status = 1 " +
            "ORDER BY p.like_count DESC LIMIT 3")
    List<ForumPost> selectTop3Posts();

    //根据用户名查询帖子
    @Select("SELECT p.*, u.username, u.avatar_url AS avatarUrl FROM forum_post p " +
            "JOIN users u ON p.user_id = u.user_id " +
            "WHERE u.username = #{username} AND p.status = 1 " +
            "ORDER BY p.create_time DESC")
    List<ForumPost> selectPostsByUsername(@Param("username") String username);

    //根据关注列表查询帖子
    @Select("SELECT p.*, u.username, u.avatar_url AS avatarUrl FROM forum_post p " +
            "JOIN users u ON p.user_id = u.user_id " +
            "JOIN user_follows f ON p.user_id = f.following_id " +
            "WHERE f.follower_id = #{userId} AND p.status = 1 " +
            "ORDER BY p.create_time DESC")
    List<ForumPost> selectPostsByFollowing(@Param("userId") Integer userId);

    //查询用户已发布的帖子
    @Select("SELECT p.*, u.username, u.avatar_url AS avatarUrl FROM forum_post p " +
            "LEFT JOIN users u ON p.user_id = u.user_id " +
            "WHERE p.user_id = #{userId} AND p.status = 1 " +
            "ORDER BY p.create_time DESC")
    List<ForumPost> selectPublishedPostsByUserId(@Param("userId") Integer userId);

    //查询用户收藏的帖子
    @Select("SELECT p.*, u.username, u.avatar_url AS avatarUrl FROM forum_post p " +
            "JOIN forum_post_favorite f ON p.post_id = f.post_id " +
            "JOIN users u ON p.user_id = u.user_id " +
            "WHERE f.user_id = #{userId} AND p.status = 1 " +
            "ORDER BY f.create_time DESC")
    List<ForumPost> selectFavoritePostsByUserId(@Param("userId") Integer userId);

    //更新帖子状态
    @Update("UPDATE forum_post SET status = #{status} WHERE post_id = #{postId}")
    int updatePostStatus(@Param("postId") Integer postId, @Param("status") Integer status);

    //管理员查询所有帖子
    @Select("SELECT p.*, u.username, u.avatar_url AS avatarUrl FROM forum_post p " +
            "LEFT JOIN users u ON p.user_id = u.user_id " +
            "ORDER BY p.create_time DESC")
    List<ForumPost> selectAllPosts();

    //查询所有分类
    @Select("SELECT * FROM forum_category WHERE status = 1 ORDER BY sort_order")
    List<com.example.springboot.entity.ForumCategory> selectAll();

    //查询是否收藏
    @Select("SELECT * FROM forum_post_favorite WHERE user_id = #{userId}")
    List<ForumPostFavorite> findPostStatus(@Param("userId") Integer userId);
}
