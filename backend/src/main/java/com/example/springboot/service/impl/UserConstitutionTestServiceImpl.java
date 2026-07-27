package com.example.springboot.service.impl;

import com.example.springboot.entity.UserConstitutionTests;
import com.example.springboot.mapper.UserConstitutionTestsMapper;
import com.example.springboot.req.ConstitutionTestReq;
import com.example.springboot.res.ConstitutionTestRes;
import com.example.springboot.service.UserConstitutionTestService;
import com.example.springboot.utils.AiOutputGuard;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserConstitutionTestServiceImpl implements UserConstitutionTestService {

    @Autowired
    private UserConstitutionTestsMapper testsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConstitutionTestRes submitTest(ConstitutionTestReq req) {
        UserConstitutionTests test;
        boolean updateExisting = req.getTestId() != null;

        if (updateExisting) {
            test = testsMapper.selectById(req.getTestId());
            if (test == null) {
                throw new IllegalArgumentException("constitution test not found: " + req.getTestId());
            }
            if (req.getUserId() != null && !req.getUserId().equals(test.getUserId())) {
                throw new IllegalArgumentException("constitution test does not belong to user: " + req.getUserId());
            }
        } else {
            test = new UserConstitutionTests();
            test.setUserId(req.getUserId());
            test.setTestTime(LocalDateTime.now());
        }

        applyCoreFields(test, req);
        if (hasValidAdviceFields(req)) {
            applyAdviceFields(test, req);
        }
        test.setUpdateTime(LocalDateTime.now());

        if (updateExisting) {
            testsMapper.updateById(test);
        } else {
            testsMapper.insert(test);
        }

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
        List<UserConstitutionTests> list = testsMapper.selectHistoryByUserId(userId);
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

    private void applyCoreFields(UserConstitutionTests test, ConstitutionTestReq req) {
        test.setConstitutionScores(req.getConstitutionScores());
        test.setConstitutionConvertScores(req.getConstitutionConvertScores());
        test.setMainConstitution(req.getMainConstitution());
        test.setSubConstitution(req.getSubConstitution());
        test.setSuggest(req.getSuggest());
    }

    private void applyAdviceFields(UserConstitutionTests test, ConstitutionTestReq req) {
        test.setPlan(req.getPlan());
        test.setRegion(req.getRegion());
        test.setAgeAvoid(req.getAgeAvoid());
        test.setAgeFocus(req.getAgeFocus());
        test.setFoodStaple(req.getFoodStaple());
        test.setFoodVeg(req.getFoodVeg());
        test.setFoodMeat(req.getFoodMeat());
        test.setDaily(req.getDaily());
    }

    private boolean hasValidAdviceFields(ConstitutionTestReq req) {
        String joined = String.join("\n",
                nullToEmpty(req.getPlan()),
                nullToEmpty(req.getRegion()),
                nullToEmpty(req.getAgeAvoid()),
                nullToEmpty(req.getAgeFocus()),
                nullToEmpty(req.getFoodStaple()),
                nullToEmpty(req.getFoodVeg()),
                nullToEmpty(req.getFoodMeat()),
                nullToEmpty(req.getDaily())
        );
        if (!AiOutputGuard.hasText(joined) || AiOutputGuard.isFailureText(joined)) {
            return false;
        }
        return AiOutputGuard.hasText(req.getPlan())
                && AiOutputGuard.hasText(req.getFoodStaple())
                && AiOutputGuard.hasText(req.getFoodVeg())
                && AiOutputGuard.hasText(req.getFoodMeat())
                && AiOutputGuard.hasText(req.getDaily());
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
