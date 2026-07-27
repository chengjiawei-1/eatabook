package com.example.springboot.service.impl;

import com.example.springboot.entity.UserMealPlan;
import com.example.springboot.mapper.UserMealPlanMapper;
import com.example.springboot.service.UserMealPlanService;
import com.example.springboot.utils.AiOutputGuard;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserMealPlanServiceImpl implements UserMealPlanService {

    @Autowired
    private UserMealPlanMapper userMealPlanMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> getMealPlan(Integer userId, String currentConstitution) {
        UserMealPlan plan = userMealPlanMapper.selectByUserId(userId);

        if (plan != null) {
            if (!isValidMealPlanJson(plan.getPlanJson())) {
                userMealPlanMapper.deleteByUserId(userId);
                return null;
            }

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
        if (!isValidMealPlanJson(planJson)) {
            throw new IllegalArgumentException("invalid meal plan json");
        }

        UserMealPlan plan = new UserMealPlan();
        plan.setUserId(userId);
        plan.setPlanJson(planJson);
        plan.setBaseConstitution(constitution);
        plan.setGenerateDate(new Date());

        userMealPlanMapper.saveOrUpdatePlan(plan);
    }

    private boolean isValidMealPlanJson(String planJson) {
        if (!AiOutputGuard.hasText(planJson) || AiOutputGuard.isFailureText(planJson)) {
            return false;
        }
        try {
            JsonNode root = objectMapper.readTree(planJson);
            return hasValidMealArray(root, "breakfast")
                    && hasValidMealArray(root, "lunch")
                    && hasValidMealArray(root, "dinner");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasValidMealArray(JsonNode root, String field) {
        JsonNode array = root == null ? null : root.get(field);
        if (array == null || !array.isArray() || array.size() < 7) {
            return false;
        }
        for (JsonNode item : array) {
            if (!item.canConvertToInt() || item.asInt() <= 0) {
                return false;
            }
        }
        return true;
    }
}
