package com.example.springboot.service;

import com.example.springboot.entity.*;

import java.util.ArrayList;
import java.util.List;

public interface ForumService {

    // 帖子分类
    List<ForumCategory> listCategories();

    // 帖子列表
    List<ForumPost> listPosts(Integer categoryId, String keyword);

    // 根据帖子id获取帖子详情
    ForumPost getPostDetail(Integer postId, Integer currentUserId);

    // 批量插入帖子图片
    int uploadPostImages(Integer postId, List<String> imageUrls);

    // 更新帖子封面图
    int updateCoverImage(Integer postId, String coverImageUrl);

    // 根据帖子id查询图片
    List<PostImage> getPostImages(Integer postId);

    // 发布帖子
    int publishPost(ForumPost post);

    // 帖子点赞
    boolean togglePostLike(Integer postId, Integer userId);

    // 帖子收藏
    boolean togglePostFavorite(Integer postId, Integer userId);

    // 发布评论
    int publishComment(ForumComment comment);

    // 根据帖子id查询评论
    List<ForumComment> listComments(Integer postId, Integer pageNum, Integer pageSize);

    // 评论点赞
    boolean toggleCommentLike(Integer commentId, Integer userId);

    // 获取评论
    ForumComment getCommentById(Integer commentId);

    // 删除评论
    int deleteComment(Integer commentId);

    // 删除帖子
    int deletePost(Integer postId);

    // 保存搜索记录
    int saveSearchRecord(ForumsearchRecord record);

    // 根据用户ID查询搜索记录
    List<ForumsearchRecord> getSearchRecordsByUserId(Integer userId);

    // 查询点赞量前3帖子
    List<ForumPost> getTop3Posts();

    // 查询用户帖子
    List<ForumPost> listUserPosts(Integer userId, Integer status);

    // 删除用户全部搜索记录
    int deleteUserSearchRecords(Integer userId);

    // 删除特定搜索记录
    int deleteSearchRecordById(Integer searchId);

    // 删除用户特定搜索内容记录
    int deleteSearchRecordByContent(Integer userId, String searchContent);

    // 更新帖子状态
    int updatePostStatus(Integer postId, Integer status);

    // 管理员查询所有帖子
    List<ForumPost> listAllPostsForAdmin();

    // 热点推荐
    List<ForumPost> getHotPosts(int limit, Integer lastPostId);

    // 个性化推荐
    List<ForumPost> getPersonalizedPosts(Integer userId, int limit, Integer lastPostId);

    // 最新推荐
    List<ForumPost> getLatestPosts(int limit, Integer lastPostId);

    // 探索推荐
    List<ForumPost> getExplorePosts(int limit, Integer lastPostId);

    // 根据 ID 列表获取帖子
    List<ForumPost> getPostsByIds(List<Integer> postIds);

    // 核心对外接口
    List<ForumPost> getMixedRecommendPosts(
            Integer userId,
            int limit,
            Integer lastPostId
    );

    //不符合混合推荐帖子
    List<ForumPost> getRemainingPosts(List<Integer> excludeIds, int limit, Integer lastPostId);

    // 清除推荐缓存
    void clearRecentRecommendations();

    // 根据用户名查询帖子
    List<ForumPost> getPostsByUsername(String username);

    // 根据关注列表查询帖子
    List<ForumPost> getPostsByFollowing(Integer userId);

    // 查询指定用户已发布（审核通过）的帖子
    List<ForumPost> getPublishedPostsByUserId(Integer userId);

    // 查询用户收藏的帖子
    List<ForumPost> getFavoritePostsByUserId(Integer userId);

    List<Integer> findPostStatus(Integer userId);
}