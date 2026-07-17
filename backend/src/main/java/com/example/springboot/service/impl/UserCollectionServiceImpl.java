package com.example.springboot.service.impl;

import com.example.springboot.entity.UserCollections;
import com.example.springboot.mapper.UserCollectionMapper;
import com.example.springboot.service.UserCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserCollectionServiceImpl implements UserCollectionService {

    @Autowired
    private UserCollectionMapper userCollectionMapper;
    @Override
    public int insert(UserCollections userCollections) {
        LocalDateTime now = LocalDateTime.now();
        userCollections.setCollectionTime(now);
        userCollections.setUpdateTime(now);
        return userCollectionMapper.insert(userCollections);
    }
    @Override
    public int deleteByUserAndTarget(Integer userId, Integer collectionId) {
        return userCollectionMapper.deleteByUserAndTarget(userId, collectionId);
    }
    @Override
    public UserCollections selectById(Integer collectionId) {
        return userCollectionMapper.selectById(collectionId);
    }
    @Override
    public List<UserCollections> selectByUserId(Integer userId) {
        return userCollectionMapper.selectByUserId(userId);
    }
    @Override
    public List<UserCollections> selectByUserAndType(Integer userId, String targetType) {
        return userCollectionMapper.selectByUserAndType(userId, targetType);
    }
    @Override
    public int countByUserAndType(Integer userId, String targetType) {
        return userCollectionMapper.countByUserAndType(userId, targetType);
    }
    @Override
    public int countByType(String targetType) {
        return userCollectionMapper.countByType(targetType);
    }
    @Override
    public int updateTime(Integer collectionId, LocalDateTime updateTime) {
        return userCollectionMapper.updateTime(collectionId, updateTime);
    }
}
