package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface UserService {
    int insert(User user);
    User selectById(Integer id);
    List<User> selectAll();
    User selectByUsername(String username);
    User selectByEmail(String email);
    User selectByPhone(String phone);
    int update(User user);
    int updateAvatar(Integer userId, String avatarUrl, LocalDateTime updateTime);
    int updatePassword(Integer id, String password, LocalDateTime updateTime);
    int delete(Integer id);
    boolean updateLastLoginTime(Integer id, LocalDateTime loginTime);
    boolean setReminder(Integer userId, LocalDateTime time);
    int addPoints(Integer userId, Integer amount);
    int updateContent(Integer userId, String content);
}

