package com.example.springboot.service.impl;

import com.example.springboot.entity.UserMealPlan;
import com.example.springboot.mapper.UserMealPlanMapper;
import com.example.springboot.service.UserMealPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserMealPlanServiceImpl implements UserMealPlanService {

    @Autowired
    private UserMealPlanMapper userMealPlanMapper;
    @Override
    public Map<String, Object> getMealPlan(Integer userId, String currentConstitution) {
        UserMealPlan plan = userMealPlanMapper.selectByUserId(userId);

        if (plan != null) {
            String dbConst = plan.getBaseConstitution().trim();

            boolean isConstitutionSame = currentConstitution != null && currentConstitution.trim().equals(dbConst);


            long diffMillis = System.currentTimeMillis() - plan.getGenerateDate().getTime();
            long diffDays = diffMillis / (1000 * 3600 * 24);
            boolean isWithinSevenDays = diffDays < 7;

            if (isConstitutionSame && isWithinSevenDays) {

                Map<String, Object> result = new HashMap<>();
                result.put("generateDate", plan.getGenerateDate());
                result.put("planJson", plan.getPlanJson());
                return result;
            }
        }
        return null;
    }

    @Override
    public void savePlan(Integer userId, String planJson, String constitution) {
        UserMealPlan plan = new UserMealPlan();
        plan.setUserId(userId);
        plan.setPlanJson(planJson);
        plan.setBaseConstitution(constitution);
        plan.setGenerateDate(new Date());

        userMealPlanMapper.saveOrUpdatePlan(plan);
    }
}