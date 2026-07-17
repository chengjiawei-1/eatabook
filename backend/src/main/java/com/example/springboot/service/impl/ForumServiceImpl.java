package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.entity.*;
import com.example.springboot.mapper.*;
import com.example.springboot.service.DataDashboardService;
import com.example.springboot.service.ForumService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.Objects;
import java.util.LinkedHashMap;
import static net.sf.jsqlparser.parser.feature.Feature.comment;



@Service
public class ForumServiceImpl implements ForumService {

    @Autowired
    private ForumCategoryMapper categoryMapper;
    @Autowired
    private ForumPostMapper postMapper;
    @Autowired
    private ForumCommentMapper commentMapper;
    @Autowired
    private ForumPostLikeMapper postLikeMapper;
    @Autowired
    private ForumPostFavoriteMapper postFavoriteMapper;
    @Autowired
    private ForumCommentLikeMapper commentLikeMapper;
    @Autowired
    private ForumSearchMapper forumSearchMapper;
    @Autowired
    private DataDashboardService dataDashboardService;
    @Autowired
    private ForumRecommendMapper forumRecommendMapper;

    private final Set<Integer> recentlyRecommended = new HashSet<>();
    private final Object lock = new Object();

    @Override
    public List<ForumCategory> listCategories() {

        return categoryMapper.selectAll();
    }

    @Override
    public List<ForumPost> listPosts(Integer categoryId, String keyword) {
        return postMapper.selectPost(categoryId, keyword, 1); // 添加status参数
    }

    @Override
    public List<ForumPost> listUserPosts(Integer userId, Integer status) {
        return postMapper.selectUserPosts(userId, status);
    }

    //根据用户名查询帖子
    @Override
    public List<ForumPost> getPostsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return List.of();
        }
        return postMapper.selectPostsByUsername(username);
    }

    //根据关注列表查询帖子
    @Override
    public List<ForumPost> getPostsByFollowing(Integer userId) {
        if (userId == null) {
            return List.of();
        }
        return postMapper.selectPostsByFollowing(userId);
    }

    //查询我发布通过审核的帖子
    @Override
    public List<ForumPost> getPublishedPostsByUserId(Integer userId) {
        if (userId == null) {
            return List.of();
        }
        return postMapper.selectPublishedPostsByUserId(userId);
    }

    //查询用户收藏的帖子
    @Override
    public List<ForumPost> getFavoritePostsByUserId(Integer userId) {
        if (userId == null) {
            return List.of();
        }
        return postMapper.selectFavoritePostsByUserId(userId);
    }

    //更新封面图
    @Override
    public int updateCoverImage(Integer postId, String coverImageUrl) {
        return postMapper.updateCoverImage(postId, coverImageUrl);
    }

    //根据id查询图片
    @Override
    public List<PostImage> getPostImages(Integer postId) {
        return postMapper.selectImagesByPostId(postId);
    }

    @Override
    public ForumPost getPostDetail(Integer postId, Integer currentUserId) {
        ForumPost post = postMapper.selectByIdWithStatus(postId, currentUserId);

        if (post == null) {
            return null;
        }

        List<PostImage> images = postMapper.selectImagesByPostId(postId);
        List<String> imageUrls = new ArrayList<>();
        for (PostImage img : images) {
            imageUrls.add(img.getImageUrl());
        }
        post.setImages(imageUrls);

        return post;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int publishPost(ForumPost post) {
        LocalDateTime now = LocalDateTime.now();
        post.setCreateTime(now);
        post.setUpdateTime(now);
        post.setStatus(0);
        postMapper.insert(post);

        Integer postId = post.getPostId();
        if (postId == null) {
            throw new RuntimeException("帖子插入失败，postId 为空");
        }

        System.out.println("当前数据库 = " +
                postMapper.selectById(1));

        return postId;


    }

    @Override
    @Transactional
    public boolean togglePostLike(Integer postId, Integer userId) {
        if (postLikeMapper.exists(postId, userId) > 0) {
            postLikeMapper.delete(postId, userId);
            postMapper.incLike(postId, -1);
            return false;
        } else {
            postLikeMapper.insert(postId, userId);
            postMapper.incLike(postId, 1);
            return true;
        }
    }

    @Override
    @Transactional
    public boolean togglePostFavorite(Integer postId, Integer userId) {
        if (postFavoriteMapper.exists(postId, userId) > 0) {
            postFavoriteMapper.delete(postId, userId);
            postMapper.incFavorite(postId, -1);
            return false;
        } else {
            postFavoriteMapper.insert(postId, userId);
            postMapper.incFavorite(postId, 1);
            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int publishComment(ForumComment comment) {
        LocalDateTime now = LocalDateTime.now();
        comment.setCreateTime(now);
        comment.setUpdateTime(now);
        //插入评论
        int res = commentMapper.insert(comment);
        //增加评论数
        postMapper.incComment(comment.getPostId(), 1);
        return res;
    }

    @Override
    public List<ForumComment> listComments(Integer postId, Integer pageNum, Integer pageSize) {
        // 计算分页起始位置
        int offset = (pageNum - 1) * pageSize;

        return postMapper.selectCommentsByPostIdWithPaging(postId, offset, pageSize);
    }

    @Override
    @Transactional
    public boolean toggleCommentLike(Integer commentId, Integer userId) {
        if (commentLikeMapper.exists(commentId, userId) > 0) {
            commentLikeMapper.delete(commentId, userId);
            commentMapper.incLike(commentId, -1);
            return false;
        } else {
            commentLikeMapper.insert(commentId, userId);
            commentMapper.incLike(commentId, 1);
            return true;
        }
    }

    @Override
    public ForumComment getCommentById(Integer commentId) {
        return commentMapper.selectById(commentId);
    }

    @Override
    public int deleteComment(Integer commentId) {
        ForumComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return 0;
        }
        int res = commentMapper.deleteComment(commentId);
        postMapper.incComment(comment.getPostId(), -1);
        return res;
    }

    @Override
    public int deletePost(Integer postId) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null) {
            return 0;
        }

        int res = postMapper.deletePost(postId);
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int uploadPostImages(Integer postId, List<String> imageUrls) {

        if (imageUrls == null || imageUrls.isEmpty()) {
            return 0;
        }

        List<PostImage> images = new ArrayList<>();

        for (int i = 0; i < imageUrls.size(); i++) {
            PostImage img = new PostImage();
            img.setPostId(postId);
            img.setImageUrl(imageUrls.get(i));
            img.setSortOrder(i);
            img.setImageType(i == 0 ? Integer.valueOf(1) : Integer.valueOf(0));
            img.setStatus(1);
            img.setCreateTime(LocalDateTime.now());
            images.add(img);
        }

        // 批量插入图片
        int result = postMapper.batchInsertImages(images);

        // 将第一张图片设置为封面图
        if (!imageUrls.isEmpty()) {
            postMapper.updateCoverImage(postId, imageUrls.get(0));
        }

        return result;
    }

    //插入搜索记录
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveSearchRecord(ForumsearchRecord record) {
        // 设置时间戳
        LocalDateTime now = LocalDateTime.now();
        record.setSearchTime(now);
        record.setUpdateTime(now);
        //保存搜索记录
        int result = forumSearchMapper.insert(record);

        // 实时分词处理
        if (result > 0 && record.getSearchContent() != null) {
            try {
                dataDashboardService.processSearchContent(record.getSearchContent());
            } catch (Exception e) {
                // 分词失败不影响搜索记录保存
                System.out.println("分词处理失败: " + e.getMessage());
            }
        }

        return result;
    }

    //查询对应用户搜索记录
    @Override
    public List<ForumsearchRecord> getSearchRecordsByUserId(Integer userId) {
        return forumSearchMapper.selectByUserId(userId);
    }

    @Override
    public int deleteUserSearchRecords(Integer userId) {
        return forumSearchMapper.deleteByUserId(userId);
    }

    @Override
    public int deleteSearchRecordById(Integer searchId) {
        return forumSearchMapper.deleteById(searchId);
    }

    @Override
    public int deleteSearchRecordByContent(Integer userId, String searchContent) {
        return forumSearchMapper.deleteByUserIdAndContent(userId, searchContent);
    }

    //查询点赞量前三帖子
    @Override
    public List<ForumPost> getTop3Posts() {
        return postMapper.selectTop3Posts();
    }

    @Override
    public int updatePostStatus(Integer postId, Integer status) {
        return postMapper.updatePostStatus(postId, status);
    }


    @Override
    public List<ForumPost> listAllPostsForAdmin() {
        return postMapper.selectAllPosts();
    }

    //帖子推荐相关
    // 热点推荐
    @Override
    public List<ForumPost> getHotPosts(int limit, Integer lastPostId) {
        return forumRecommendMapper.selectHotPosts(limit, lastPostId);
    }

    // 个性化推荐
    @Override
    public List<ForumPost> getPersonalizedPosts(
            Integer userId,
            int limit,
            Integer lastPostId
    ) {
        if (userId == null) {
            return getHotPosts(limit, lastPostId);
        }
        return forumRecommendMapper
                .selectPersonalizedPosts(userId, limit, lastPostId);
    }

    // 最新推荐
    @Override
    public List<ForumPost> getLatestPosts(int limit, Integer lastPostId) {
        return forumRecommendMapper.selectLatestPosts(limit, lastPostId);
    }

    // 探索推荐
    @Override
    public List<ForumPost> getExplorePosts(int limit, Integer lastPostId) {
        return forumRecommendMapper.selectExplorePosts(limit, lastPostId);
    }

    // ID 查询
    @Override
    public List<ForumPost> getPostsByIds(List<Integer> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }
        return forumRecommendMapper.selectPostsByIds(postIds);
    }

    //不符合混合推荐帖子
    @Override
    public List<ForumPost> getRemainingPosts(
            List<Integer> excludeIds,
            int limit,
            Integer lastPostId
    ) {
        if (excludeIds == null) {
            excludeIds = Collections.emptyList();
        }

        return forumRecommendMapper.selectRemainingPosts(
                excludeIds,
                limit,
                lastPostId
        );
    }

    @Override
    public List<ForumPost> getMixedRecommendPosts(Integer userId, int limit, Integer lastPostId) {
        try {
            if (limit <= 0) {
                limit = 200;
            }

            System.out.println("========== 推荐开始 ==========");
            System.out.println("【推荐】userId = " + userId);
            System.out.println("【推荐】limit = " + limit);
            System.out.println("【推荐】lastPostId = " + lastPostId);

            // 推荐比例分配
            int personalizedCount = (int) (limit * 0.4);
            int hotCount = (int) (limit * 0.3);
            int latestCount = (int) (limit * 0.2);
            int exploreCount = limit - personalizedCount - hotCount - latestCount;

            // 防御性处理
            personalizedCount = Math.max(0, personalizedCount);
            hotCount = Math.max(0, hotCount);
            latestCount = Math.max(0, latestCount);
            exploreCount = Math.max(0, exploreCount);

            System.out.println("【推荐】比例分配 -> 个性化:" + personalizedCount
                    + " 热点:" + hotCount + " 最新:" + latestCount + " 探索:" + exploreCount);

            LinkedHashMap<Integer, ForumPost> result = new LinkedHashMap<>();
            List<Integer> alreadyIncluded = new ArrayList<>();

            // 个性化推荐
            if (userId != null && personalizedCount > 0) {
                List<ForumPost> personalized = forumRecommendMapper.selectPersonalizedPosts(
                        userId, personalizedCount, lastPostId
                );
                System.out.println("【推荐】个性化返回：" + personalized.size());
                for (ForumPost post : personalized) {
                    if (result.putIfAbsent(post.getPostId(), post) != null) {
                        alreadyIncluded.add(post.getPostId());
                    }
                }
            }

            // 热点推荐
            if (hotCount > 0) {
                List<ForumPost> hot = forumRecommendMapper.selectHotPosts(hotCount, lastPostId);
                System.out.println("【推荐】热点返回：" + hot.size());
                for (ForumPost post : hot) {
                    if (result.putIfAbsent(post.getPostId(), post) != null) {
                        alreadyIncluded.add(post.getPostId());
                    }
                }
            }

            // 最新推荐
            if (latestCount > 0) {
                List<ForumPost> latest = forumRecommendMapper.selectLatestPosts(latestCount, lastPostId);
                System.out.println("【推荐】最新返回：" + latest.size());
                for (ForumPost post : latest) {
                    if (result.putIfAbsent(post.getPostId(), post) != null) {
                        alreadyIncluded.add(post.getPostId());
                    }
                }
            }

            // 探索推荐
            if (exploreCount > 0) {
                List<ForumPost> explore = forumRecommendMapper.selectExplorePosts(exploreCount, lastPostId);
                System.out.println("【推荐】探索返回：" + explore.size());
                for (ForumPost post : explore) {
                    if (result.putIfAbsent(post.getPostId(), post) != null) {
                        alreadyIncluded.add(post.getPostId());
                    }
                }
            }

            // 如果推荐数量仍不足
            int currentSize = result.size();
            if (currentSize < limit) {
                int need = limit - currentSize;

                List<Integer> excludeIds = new ArrayList<>(result.keySet());

                synchronized (lock) {
                    excludeIds.addAll(recentlyRecommended);
                }

                List<ForumPost> remaining = forumRecommendMapper.selectRemainingPosts(
                        excludeIds, need, lastPostId
                );

                System.out.println("【推荐】兜底返回：" + remaining.size());

                for (ForumPost post : remaining) {
                    if (result.putIfAbsent(post.getPostId(), post) != null) {
                        synchronized (lock) {
                            recentlyRecommended.add(post.getPostId());
                            if (recentlyRecommended.size() > 1000) {
                                List<Integer> recentList = new ArrayList<>(recentlyRecommended);
                                recentlyRecommended.clear();
                                int start = Math.max(0, recentList.size() - 500);
                                for (int i = start; i < recentList.size(); i++) {
                                    recentlyRecommended.add(recentList.get(i));
                                }
                            }
                        }
                    }
                }
            }

            currentSize = result.size();
            if (currentSize < limit) {
                int need = limit - currentSize;
                List<Integer> allCurrentIds = new ArrayList<>(result.keySet());

                List<ForumPost> randomPosts = forumRecommendMapper.selectRandomPosts(allCurrentIds, need);

                System.out.println("【推荐】随机补充：" + randomPosts.size());

                for (ForumPost post : randomPosts) {
                    result.putIfAbsent(post.getPostId(), post);
                }
            }

            System.out.println("【推荐】最终总数量：" + result.size());
            System.out.println("========== 推荐结束 ==========");

            return new ArrayList<>(result.values());

        } catch (Exception e) {
            System.err.println("【推荐】发生异常");
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public void clearRecentRecommendations() {
        synchronized (lock) {
            recentlyRecommended.clear();
        }
    }

    @Override
    public List<Integer> findPostStatus(Integer userId) {
        List<ForumPostFavorite> postStatus = postMapper.findPostStatus(userId);
        List<Integer> ColumnValues = new ArrayList<>();
        for (ForumPostFavorite post : postStatus) {
            ColumnValues.add(post.getPostId());
        }
        System.out.println("666666666666666");
        System.out.println(ColumnValues);
        return ColumnValues;
    }
}