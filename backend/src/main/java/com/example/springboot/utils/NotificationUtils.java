package com.example.springboot.utils;

import com.example.springboot.entity.Notification;

import java.time.LocalDateTime;
public class NotificationUtils{

    //用户被关注通知
    public static Notification followNotification(Integer followerId, Integer targetUserId) {
        Notification notification = new Notification();
        notification.setUserId(targetUserId.longValue());
        notification.setType("follow");
        notification.setTitle("有人关注了你");
        notification.setContent("用户 " + followerId + " 关注了你");
        notification.setLink("/user/" + followerId);
        notification.setRelatedId(followerId.longValue());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadStatus(false);
        return notification;
    }

    //帖子被点赞通知
    public static Notification postLikeNotification(Integer postId, Integer likerId, Integer authorId, String postTitle) {
        Notification notification = new Notification();
        notification.setUserId(authorId.longValue());
        notification.setType("post_like");
        notification.setTitle("你的帖子被点赞");
        notification.setContent("用户 " + likerId + " 点赞了你的帖子：" + postTitle);
        notification.setLink("/forum/posts/" + postId);
        notification.setRelatedId(postId.longValue());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadStatus(false);
        return notification;
    }

    //帖子被收藏通知
    public static Notification postFavoriteNotification(Integer postId, Integer favoriteId, Integer authorId, String postTitle) {
        Notification notification = new Notification();
        notification.setUserId(authorId.longValue());
        notification.setType("post_favorite");
        notification.setTitle("你的帖子被收藏");
        notification.setContent("用户 " + favoriteId + " 收藏了你的帖子：" + postTitle);
        notification.setLink("/forum/posts/" + postId);
        notification.setRelatedId(postId.longValue());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadStatus(false);
        return notification;
    }


    //帖子评论通知
    public static Notification commentNotification(Integer postId, Integer commenterId, Integer authorId, String commentContent) {
        Notification notification = new Notification();
        notification.setUserId(authorId.longValue());
        notification.setType("comment");
        notification.setTitle("你的帖子有新评论");
        notification.setContent("用户 " + commenterId + " 评论了你的帖子：" + commentContent);
        notification.setLink("/forum/posts/" + postId);
        notification.setRelatedId(postId.longValue());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadStatus(false);
        return notification;
    }

    //评论被回复通知
    public static Notification replyNotification(Integer postId, Integer replyUserId, Integer originalCommentUserId, String commentContent, Integer commentId) {
        Notification notification = new Notification();
        notification.setUserId(originalCommentUserId.longValue());
        notification.setType("comment_reply");
        notification.setTitle("你的评论被回复");
        notification.setContent("用户 " + replyUserId + " 回复了你的评论：" + commentContent);
        notification.setLink("/forum/posts/" + postId);
        notification.setRelatedId(commentId.longValue());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadStatus(false);
        return notification;
    }

    //评论被点赞通知
    public static Notification commentLikeNotification(Integer commentId, Integer likerId, Integer commentAuthorId, String commentContent) {
        Notification notification = new Notification();
        notification.setUserId(commentAuthorId.longValue());
        notification.setType("comment_like");
        notification.setTitle("你的评论被点赞");
        notification.setContent("用户 " + likerId + " 点赞了你的评论：" + commentContent);
        notification.setLink("/forum/posts/" + commentId);
        notification.setRelatedId(commentId.longValue());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadStatus(false);
        return notification;
    }

}
