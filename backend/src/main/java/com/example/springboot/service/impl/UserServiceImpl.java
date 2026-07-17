package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.config.RabbitMQConfig;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Override
    public int insert(User user) {
        return userMapper.insert(user);
    }

    @Override
    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<User> selectAll() {
        return userMapper.selectAll();
    }

    @Override
    public User selectByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User selectByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public User selectByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public int update(User user) {
        return userMapper.update(user);
    }

    @Override
    public int updateAvatar(Integer userId, String avatarUrl, LocalDateTime updateTime) {
        try {

            if (userId == null || userId <= 0) {
                throw new IllegalArgumentException("用户ID不能为空或无效");
            }
            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("头像URL不能为空");
            }


            int result = userMapper.updateAvatar(userId, avatarUrl, updateTime);

            if (result > 0) {
                log.info("用户 {} 头像更新成功", userId);
            } else {
                log.warn("用户 {} 头像更新失败，可能用户不存在", userId);
            }

            return result;
        } catch (Exception e) {
            log.error("更新用户头像失败 - userId: {}, error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("头像更新失败: " + e.getMessage());
        }
    }

    @Override
    public int updatePassword(Integer id, String password, LocalDateTime updateTime) {
        return userMapper.updatePassword(id, password, updateTime);
    }

    @Override
    public int delete(Integer id) {
        return userMapper.delete(id);
    }

    @Override
    public boolean updateLastLoginTime(Integer id, LocalDateTime loginTime) {
        return userMapper.updateLastLoginTime(id, loginTime) > 0;
    }

    @Override
    public boolean setReminder(Integer userId, LocalDateTime time) {
        int rows = userMapper.updateNextTestTime(userId, time);
        if (rows > 0) {
            if (rabbitTemplate != null) {
                try {
                    long delayTimes = Duration.between(LocalDateTime.now(), time).toMillis();

                    if (delayTimes > 0) {
                        rabbitTemplate.convertAndSend(
                                RabbitMQConfig.DELAY_EXCHANGE_NAME,
                                RabbitMQConfig.ROUTING_KEY,
                                userId,
                                message -> {
                                    message.getMessageProperties().setExpiration(String.valueOf(delayTimes));
                                    return message;
                                }
                        );
                    }
                } catch (Exception e) {
                    System.err.println("RabbitMQ消息发送失败: " + e.getMessage());
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int addPoints(Integer userId, Integer amount) {
        return userMapper.addPoints(userId,amount);
    }

    @Override
    public int updateContent(Integer userId, String content) {
        return userMapper.updateContent(userId, content);
    }
}
