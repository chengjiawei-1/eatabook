package com.example.springboot.service.impl;

import com.example.springboot.entity.Dishes;
import com.example.springboot.entity.UserConstitutionTests;
import com.example.springboot.mapper.DishesMapper;
import com.example.springboot.mapper.UserConstitutionTestsMapper;
import com.example.springboot.req.ConstitutionTestReq;
import com.example.springboot.res.ConstitutionTestRes;
import com.example.springboot.service.UserConstitutionTestService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserConstitutionTestServiceImpl implements UserConstitutionTestService {

    @Autowired
    private UserConstitutionTestsMapper testsMapper;

    @Autowired
    private DishesMapper dishesMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConstitutionTestRes submitTest(ConstitutionTestReq req) {
        // 保存测试结果
        UserConstitutionTests test = new UserConstitutionTests();
        BeanUtils.copyProperties(req, test);

        test.setTestTime(LocalDateTime.now());
        test.setUpdateTime(LocalDateTime.now());

        test.setConstitutionScores(req.getConstitutionScores());
        test.setConstitutionConvertScores(req.getConstitutionConvertScores());

        testsMapper.insert(test);

        ConstitutionTestRes res = new ConstitutionTestRes();
        res.setTestId(test.getTestId());
        res.setMsg("保存成功");
        return res;
    }

    @Override
    public List<UserConstitutionTests> getHistory(Integer userId) {
        return testsMapper.selectHistoryByUserId(userId);
    }

    @Override
    public List<UserConstitutionTests> selectByUserIdForArgent(Integer userId) {
        return testsMapper.selectByUserIdForArgent(userId);
    }

    @Override
    public PageInfo<UserConstitutionTests> getHistoryByPage(Integer pageNo, Integer pageSize, Integer userId) {
        PageHelper.startPage(pageNo, pageSize);
        List<UserConstitutionTests> list =testsMapper.selectHistoryByUserId(userId);
        return new PageInfo<>(list);
    }

    @Override
    public Integer totalcount() {
        return testsMapper.totalTests();
    }
    @Override
    public Integer todaycount() {
        return testsMapper.todayTests();
    }
    @Override
    public Integer yesterdaycount() {
        return testsMapper.yesterdayTests();
    }
}
