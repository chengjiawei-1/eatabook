package com.example.springboot.service;

import com.example.springboot.VO.DishVideoVO;
import com.example.springboot.entity.UserPointLog;

import java.util.List;

public interface DishVideoService {
    //获取视频列表
    List<DishVideoVO> getVideosByDishId(Long dishId, Integer userId);

    //解锁视频
    boolean unlockVideo(Integer userId, Long videoId);

    int insertPointLog(UserPointLog log);
}