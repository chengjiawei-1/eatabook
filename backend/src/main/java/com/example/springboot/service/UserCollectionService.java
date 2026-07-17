package com.example.springboot.service;

import com.example.springboot.entity.UserCollections;
import java.time.LocalDateTime;
import java.util.List;

public interface UserCollectionService {

    int insert(UserCollections userCollections);

    int deleteByUserAndTarget(Integer userId, Integer collectionId);

    UserCollections selectById(Integer collectionId);

    List<UserCollections> selectByUserId(Integer userId);

    List<UserCollections> selectByUserAndType(Integer userId, String targetType);

    int countByUserAndType(Integer userId, String targetType);

    int countByType(String targetType);

    int updateTime(Integer collectionId, LocalDateTime updateTime);
}
