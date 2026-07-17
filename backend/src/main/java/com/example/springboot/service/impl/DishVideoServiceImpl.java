package com.example.springboot.service.impl;

import com.example.springboot.entity.DishVideo;
import com.example.springboot.entity.User;
import com.example.springboot.entity.UserPointLog;
import com.example.springboot.entity.UserVideoUnlock;
import com.example.springboot.mapper.DishVideoMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.DishVideoService;
import com.example.springboot.VO.DishVideoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DishVideoServiceImpl implements DishVideoService {

    @Autowired
    private DishVideoMapper dishVideoMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<DishVideoVO> getVideosByDishId(Long dishId, Integer userId) {
        // 获取该菜品所有视频
        List<DishVideo> videos = dishVideoMapper.selectByDishId(dishId);
        List<DishVideoVO> voList = new ArrayList<>();

        for (DishVideo video : videos) {
            DishVideoVO vo = new DishVideoVO();
            BeanUtils.copyProperties(video, vo);

            boolean isUnlocked = false;

            // 判断解锁状态
            if (video.getPrice() == 0) {
                isUnlocked = true;
            } else if (userId != null) {
                // 查询数据库是否有解锁记录
                int count = dishVideoMapper.countUnlock(userId, video.getId());
                if (count > 0) isUnlocked = true;
            }

            vo.setIsUnlocked(isUnlocked);

            // 如果未解锁，隐藏真实播放地址
            if (!isUnlocked) {
                vo.setVideoUrl(null);
            }

            voList.add(vo);
        }
        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlockVideo(Integer userId, Long videoId) {

        DishVideo video = dishVideoMapper.selectById(videoId);
        if (video == null) throw new RuntimeException("视频不存在");


        if (dishVideoMapper.countUnlock(userId, videoId) > 0) {
            return true;
        }


        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");

        int currentPoints = user.getPoints() == null ? 0 : user.getPoints();

        if (currentPoints < video.getPrice()) {
            throw new RuntimeException("积分不足，请先获取积分");
        }

        int newBalance = currentPoints - video.getPrice();

        userMapper.updatePoints(userId, newBalance);


        UserPointLog log = new UserPointLog();
        log.setUserId(userId);
        log.setType(2);
        log.setAmount(video.getPrice());
        log.setBalance(newBalance);
        log.setDescription("解锁视频：" + video.getTitle());
        log.setCreateTime(LocalDateTime.now());
        dishVideoMapper.insertPointLog(log);

        UserVideoUnlock unlock = new UserVideoUnlock();
        unlock.setUserId(userId.longValue());
        unlock.setVideoId(videoId);
        unlock.setUnlockTime(LocalDateTime.now());
        dishVideoMapper.insertUnlock(unlock);

        return true;
    }

    @Override
    public int insertPointLog(UserPointLog log) {
        if (log.getCreateTime() == null) {
            log.setCreateTime(LocalDateTime.now());
        }
        return dishVideoMapper.insertPointLog(log);
    }
}