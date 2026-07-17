package com.example.springboot.controller;

import com.example.springboot.entity.*;
import com.example.springboot.service.ForumService;
import com.example.springboot.service.NotificationService;
import com.example.springboot.utils.JwtUtils;
import com.example.springboot.utils.NotificationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
@CrossOrigin
@Tag(name = "ForumController", description = "论坛帖子评论相关接口")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private NotificationService notificationService;


    // 统一返回结果封装
    private Map<String, Object> resultSuccess(Object data) {
        return resultSuccess(data, "操作成功");
    }

    private Map<String, Object> resultSuccess(Object data, String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", msg);
        map.put("data", data);
        return map;
    }

    private Map<String, Object> resultError(String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 500);
        map.put("msg", msg);
        map.put("data", null);
        return map;
    }

    //解析Token
    private Integer parseToken(String token) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new Exception("Token不能为空");
        }
        String realToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return JwtUtils.parseToken(realToken);
    }

    @Operation(summary = "按类别查询论坛", description = "根据类别查询论坛帖子")
    @GetMapping("/categories")
    public Map<String, Object> categories() {
        List<ForumCategory> list = forumService.listCategories();
        return resultSuccess(list);
    }

    @Operation(summary = "按关键词查询论坛", description = "根据关键词查询论坛帖子")
    @GetMapping("/posts")
    public Map<String, Object> posts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword) {
        List<ForumPost> list = forumService.listPosts(categoryId, keyword);
        return resultSuccess(list);
    }

    @Operation(summary = "按用户id查询论坛", description = "根据用户id查询论坛帖子")
    @GetMapping("/posts/{id}")
    public Map<String, Object> detail(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String token) {
        Integer currentUserId = null;
        try {
            if (token != null && !token.isEmpty()) {
                currentUserId = parseToken(token);
            }
        } catch (Exception e) {

        }
        ForumPost post = forumService.getPostDetail(id, currentUserId);
        return resultSuccess(post);
    }

    //评论接口分页
    @Operation(summary = "查询论坛评论", description = "查询特定论坛的所有评论")
    @GetMapping("/posts/{postId}/comments")
    public Map<String, Object> comments(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        List<ForumComment> list = forumService.listComments(postId, pageNum, pageSize);
        return resultSuccess(list);
    }

    @Operation(summary = "查询热度前3论坛", description = "根据热度查询排行前3的论坛帖子")
    @GetMapping("/posts/top")
    public Map<String, Object> getTop3Posts() {
        List<ForumPost> list = forumService.getTop3Posts();
        return resultSuccess(list);
    }

    //根据用户名查询帖子
    @Operation(summary = "按用户名查询论坛", description = "根据用户名查询帖子")
    @GetMapping("/posts/by-username")
    public Map<String, Object> getPostsByUsername(@RequestParam String username) {
        try {
            List<ForumPost> posts = forumService.getPostsByUsername(username);
            return resultSuccess(posts);
        } catch (Exception e) {
            return resultError("查询失败: " + e.getMessage());
        }
    }

    //根据关注列表查询帖子
    @Operation(summary = "按关注列表查询论坛", description = "根据关注列表查询帖子")
    @GetMapping("/posts/following")
    public Map<String, Object> getPostsByFollowing(
            @RequestHeader("Authorization") String token) {
        try {
            Integer userId = parseToken(token);
            List<ForumPost> posts = forumService.getPostsByFollowing(userId);
            return resultSuccess(posts);
        } catch (Exception e) {
            return resultError("查询失败: " + e.getMessage());
        }
    }

    //查询当前用户已发布的帖子
    @Operation(summary = "查询当前用户已发布的帖子", description = "查询当前用户已发布的帖子")
    @GetMapping("/posts/my-published")
    public Map<String, Object> getMyPublishedPosts(
            @RequestHeader("Authorization") String token) {
        try {
            Integer userId = parseToken(token);
            List<ForumPost> posts = forumService.getPublishedPostsByUserId(userId);
            return resultSuccess(posts);
        } catch (Exception e) {
            return resultError("查询失败: " + e.getMessage());
        }
    }

    //查询当前用户收藏的帖子
    @Operation(summary = "查询当前用户收藏的帖子", description = "查询当前用户收藏的帖子")
    @GetMapping("/posts/favorites")
    public Map<String, Object> getFavoritePosts(
            @RequestHeader("Authorization") String token) {
        try {
            Integer userId = parseToken(token);
            List<ForumPost> posts = forumService.getFavoritePostsByUserId(userId);
            return resultSuccess(posts);
        } catch (Exception e) {
            return resultError("获取失败: " + e.getMessage());
        }
    }

    //需要登录的接口
    @Operation(summary = "检查用户是否登录", description = "需要登录的接口")
    @PostMapping("/posts")
    public Map<String, Object> publishPost(
            @RequestBody ForumPost post,
            @RequestHeader("Authorization") String token) {
        try {
            Integer userId = parseToken(token);
            post.setUserId(userId);
            post.setStatus(0);

            Integer postId = forumService.publishPost(post);

            Map<String, Object> data = new HashMap<>();
            data.put("postId", postId);
            return resultSuccess(data, "发布成功，等待审核");
        } catch (Exception e) {
            e.printStackTrace();
            return resultError("发布失败: " + e.getMessage());
        }
    }

    @Operation(summary = "检查自己的帖子", description = "查询自己发布的帖子")
    @GetMapping("/posts/me")
    public Map<String, Object> getMyPosts(
            @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String token) {
        try {
            Integer userId = parseToken(token);
            List<ForumPost> list = forumService.listUserPosts(userId, status);
            return resultSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            return resultError("获取失败: " + e.getMessage());
        }
    }

    //帖子点赞
    @Operation(summary = "帖子点赞", description = "帖子点赞")
    @PostMapping("/posts/{id}/likes")
    public Map<String, Object> like(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String token) {
        try {
            Integer userId = parseToken(token);

            boolean isLikedNow = forumService.togglePostLike(id, userId);

            ForumPost updatedPost = forumService.getPostDetail(id, userId);


            if (isLikedNow) {
                if (updatedPost != null && !userId.equals(updatedPost.getUserId())) {
                    Notification notification = NotificationUtils.postLikeNotification(
                            id, userId, updatedPost.getUserId(), updatedPost.getTitle()
                    );
                    notificationService.sendNotification(notification); 
                }
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("liked", isLikedNow);
            responseData.put("likeCount", updatedPost != null ? updatedPost.getLikeCount() : 0);

            return resultSuccess(responseData, isLikedNow ? "点赞成功" : "已取消点赞");
        } catch (Exception e) {
            return resultError("点赞失败: " + e.getMessage());
        }
    }

    //帖子收藏
    @Operation(summary = "帖子收藏", description = "帖子收藏")
    @PostMapping("/posts/{id}/favorites")
    public Map<String, Object> favorite(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String token) {
        try {
            Integer userId = parseToken(token);

            boolean isFavoriteNow = forumService.togglePostFavorite(id, userId);

            ForumPost updatedPost = forumService.getPostDetail(id, userId);

            if (isFavoriteNow) {
                if (updatedPost != null && !userId.equals(updatedPost.getUserId())) {
                    Notification notification = NotificationUtils.postFavoriteNotification(
                            id, userId, updatedPost.getUserId(), updatedPost.getTitle()
                    );
                    notificationService.sendNotification(notification);
                }
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("favorited", isFavoriteNow);
            responseData.put("favoriteCount", updatedPost != null ? updatedPost.getFavoriteCount() : 0);

            return resultSuccess(responseData, isFavoriteNow ? "收藏成功" : "已取消收藏");
        } catch (Exception e) {
            return resultError("收藏失败: " + e.getMessage());
        }
    }

    //发布评论/回复
    @Operation(summary = "发布评论/回复", description = "发布评论/回复")
    @PostMapping("/comments")
    public Map<String, Object> comment(
            @RequestBody ForumComment comment,
            @RequestHeader("Authorization") String token) {
        try {
            Integer userId = parseToken(token);
            comment.setUserId(userId);

            Integer commentId = forumService.publishComment(comment);
            if (commentId > 0) {
                ForumPost post = forumService.getPostDetail(comment.getPostId(), userId);

                if (post == null) {
                    return resultError("评论的帖子不存在");
                }

                if (comment.getParentId() == null) {
                    if (!userId.equals(post.getUserId())) {
                        Notification n = NotificationUtils.commentNotification(
                                comment.getPostId(), userId, post.getUserId(), comment.getContent()
                        );
                        notificationService.sendNotification(n);
                    }
                } else {
                    ForumComment parentComment = forumService.getCommentById(comment.getParentId());
                    if (parentComment != null && !userId.equals(parentComment.getUserId())) {
                        Notification n = NotificationUtils.replyNotification(
                                comment.getPostId(), userId, parentComment.getUserId(),
                                comment.getContent(), commentId
                        );
                        notificationService.sendNotification(n);
                    }
                }

                Map<String, Object> resData = new HashMap<>();
                resData.put("commentCount", post.getCommentCount());

                return resultSuccess(resData, "评论成功");
            }
            return resultError("评论失败");
        } catch (Exception e) {
            return resultError("评论出错: " + e.getMessage());
        }
    }

    //评论点赞接口
    @Operation(summary = "评论点赞接口", description = "评论点赞接口")
    @PostMapping("/comments/{id}/likes")
    public Map<String, Object> commentLike(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String token) {
        try {
            Integer userId = parseToken(token);

            boolean isLikedNow = forumService.toggleCommentLike(id, userId);

            if (isLikedNow) {
                ForumComment comment = forumService.getCommentById(id);
                if (comment != null && !userId.equals(comment.getUserId())) {
                    Notification n = NotificationUtils.commentLikeNotification(
                            id, userId, comment.getUserId(), comment.getContent()
                    );
                    notificationService.sendNotification(n);
                }
            }

            return resultSuccess(null, isLikedNow ? "操作成功" : "已取消点赞");
        } catch (Exception e) {
            return resultError("操作失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除评论", description = "删除评论接口")
    @DeleteMapping("/comments/{commentId}")
    public Map<String, Object> deleteComment(@PathVariable Integer commentId) {
        try {
            int result = forumService.deleteComment(commentId);
            boolean success = result > 0;
            return success ? resultSuccess(null, "删除成功") : resultError("删除失败");
        } catch (Exception e) {
            return resultError("删除失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除帖子", description = "删除帖子接口")
    @DeleteMapping("/posts/{postId}")
    public Map<String, Object> deletePost(@PathVariable Integer postId) {
        try {
            boolean success = forumService.deletePost(postId) > 0;
            return success ? resultSuccess(null, "删除成功") : resultError("删除失败");
        } catch (Exception e) {
            return resultError("删除失败: " + e.getMessage());
        }
    }

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String baseUrl;

    @Operation(summary = "帖子上传图片", description = "发布帖子时附带图片")
    @PostMapping("/posts/{postId}/images")
    public Map<String, Object> uploadPostImages(
            @PathVariable Integer postId,
            @RequestParam("files") MultipartFile[] files) {

        try {

            // 上传目录
            File dir = new File(uploadDir + "forum/");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            List<String> urls = new ArrayList<>();

            for (MultipartFile file : files) {

                String originalFilename = file.getOriginalFilename();

                String fileName =
                        System.currentTimeMillis()
                                + "_"
                                + (originalFilename != null
                                ? originalFilename
                                : "temp.jpg");

                File dest = new File(dir, fileName);

                file.transferTo(dest);

                // 返回图片访问地址
                String imageUrl =
                        baseUrl
                                + "/uploads/forum/"
                                + fileName;

                urls.add(imageUrl);
            }

            forumService.uploadPostImages(postId, urls);

            if (!urls.isEmpty()) {
                forumService.updateCoverImage(postId, urls.get(0));
            }

            return resultSuccess(urls, "图片上传成功");

        } catch (IOException e) {

            e.printStackTrace();

            return resultError("上传失败: " + e.getMessage());
        }
    }

    //搜索记录接口
    @Operation(summary = "存储搜索记录接口", description = "存储搜索记录接口")
    @PostMapping("/search-records")
    public Map<String, Object> saveSearchRecord(@RequestBody ForumsearchRecord record) {
        try {
            int result = forumService.saveSearchRecord(record);
            return resultSuccess(result, "保存成功");
        } catch (Exception e) {
            return resultError("保存失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询搜索记录接口", description = "查询搜索记录接口")
    @GetMapping("/search-records")
    public Map<String, Object> getSearchRecords(@RequestParam Integer userId) {
        List<ForumsearchRecord> list = forumService.getSearchRecordsByUserId(userId);
        return resultSuccess(list);
    }

    //删除用户全部搜索记录
    @Operation(summary = "删除搜索记录接口", description = "删除搜索记录接口")
    @DeleteMapping("/users/{userId}/search-records")
    public Map<String, Object> deleteUserSearchHistory(@PathVariable Integer userId) {
        try {
            int result = forumService.deleteUserSearchRecords(userId);
            if (result > 0) {
                return resultSuccess(result, "搜索记录删除成功");
            } else {
                return resultError("搜索记录删除失败");
            }
        } catch (Exception e) {
            return resultError("删除过程中出现错误: " + e.getMessage());
        }
    }

    //删除特定搜索记录
    @Operation(summary = "删除特定搜索记录接口", description = "删除特定搜索记录接口")
    @DeleteMapping("/search-records/{searchId}")
    public Map<String, Object> deleteSearchRecord(@PathVariable Integer searchId) {
        try {
            int result = forumService.deleteSearchRecordById(searchId);
            if (result > 0) {
                return resultSuccess(result, "搜索记录删除成功");
            } else {
                return resultError("搜索记录删除失败");
            }
        } catch (Exception e) {
            return resultError("删除过程中出现错误: " + e.getMessage());
        }
    }

    //删除用户特定搜索内容记录
    @Operation(summary = "删除用户搜索记录接口", description = "删除用户搜索记录接口")
    @DeleteMapping("/search-records")
    public Map<String, Object> deleteSearchRecordByContent(
            @RequestParam Integer userId,
            @RequestParam String searchContent) {
        try {
            int result = forumService.deleteSearchRecordByContent(userId, searchContent);
            if (result > 0) {
                return resultSuccess(result, "搜索记录删除成功");
            } else {
                return resultError("搜索记录删除失败");
            }
        } catch (Exception e) {
            return resultError("删除过程中出现错误: " + e.getMessage());
        }
    }

    //管理员获取所有帖子列表
    @Operation(summary = "获取所有帖子列表", description = "获取所有帖子列表")
    @GetMapping("/admin/posts")
    public Map<String, Object> listAllPostsForAdmin() {
        try {
            List<ForumPost> list = forumService.listAllPostsForAdmin();
            return resultSuccess(list);
        } catch (Exception e) {
            return resultError("获取数据失败: " + e.getMessage());
        }
    }

    //审核通过
    @Operation(summary = "审核通过帖子", description = "审核通过帖子")
    @PutMapping("/posts/{id}/approve")
    public Map<String, Object> approvePost(@PathVariable Integer id) {
        try {
            int result = forumService.updatePostStatus(id, 1);
            if (result > 0) {
                return resultSuccess(null, "帖子已审核通过并发布");
            }
            return resultError("操作失败，帖子可能不存在");
        } catch (Exception e) {
            return resultError("审核出错: " + e.getMessage());
        }
    }

    //审核驳回
    @Operation(summary = "审核驳回帖子", description = "审核驳回帖子")
    @PutMapping("/posts/{id}/reject")
    public Map<String, Object> rejectPost(@PathVariable Integer id) {
        try {
            int result = forumService.updatePostStatus(id, 2);
            if (result > 0) {
                return resultSuccess(null, "帖子已被驳回");
            }
            return resultError("操作失败");
        } catch (Exception e) {
            return resultError("操作出错: " + e.getMessage());
        }
    }

    @Operation(summary = "列出推荐帖子给用户")
    @GetMapping("/users/{userId}/favorite-post-ids")
    public Map<String, Object> findPostStatus(@PathVariable Integer userId) {
        try {
            return resultSuccess(forumService.findPostStatus(userId));
        } catch (Exception e) {
            return resultError("鏌ヨ澶辫触: " + e.getMessage());
        }
    }

    //推荐帖子
    @Operation(summary = "推荐帖子")
    @GetMapping("/recommend/posts")
    public Map<String, Object> recommendPosts(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer lastPostId
    ) {
        int pageSize = (limit == null || limit <= 0) ? 200 : limit;

        List<ForumPost> posts =
                forumService.getMixedRecommendPosts(
                        userId, pageSize, lastPostId
                );

        Map<String, Object> data = new HashMap<>();
        data.put("list", posts);
        data.put("hasMore", posts.size() == pageSize);

        return resultSuccess(data);
    }

    //清除推荐缓存
    @Operation(summary = "清楚推荐缓存")
    @DeleteMapping("/recommend/cache")
    public Map<String, Object> clearRecommendCache() {
        try {
            forumService.clearRecentRecommendations();
            return resultSuccess(null, "推荐缓存已清除");
        } catch (Exception e) {
            return resultError("清除缓存失败: " + e.getMessage());
        }
    }

}
