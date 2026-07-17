package com.example.springboot.service.impl;

import com.example.springboot.mapper.DishRecommendMapper;
import com.example.springboot.service.DishRecommendService;
import com.example.springboot.utils.SeasonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DishRecommendServiceImpl implements DishRecommendService {
    @Autowired
    private DishRecommendMapper dishRecommendMapper;

    @Override
    public List<Map<String, Object>> recommendDishes(Integer userId) {
        // 获取用户最新体质
        String mainConstitution = dishRecommendMapper.findLastTestMainConstitution(userId);
        if (mainConstitution == null) {
            mainConstitution = "平和质";
            System.out.println("用户未进行体质测试，使用默认体质: " + mainConstitution);
        } else {
            System.out.println("用户体质: " + mainConstitution);
        }

        // 获取用户收藏菜品的标签
        List<Map<String, Object>> favoriteTags = dishRecommendMapper.findUserFavoriteTags(userId);
        Set<String> userTags = extractTags(favoriteTags);
        if (userTags.isEmpty()) {
            userTags = getDefaultTags();
            System.out.println("用户无收藏菜品，使用默认标签: " + userTags);
        }

        // 获取用户所在地区
        Map<String, Object> userLocation = dishRecommendMapper.findUserLocation(userId);
        String userProvince = (userLocation != null && userLocation.get("native_place") != null)
                ? (String) userLocation.get("native_place")
                : "全国";

        // 获取当前季节
        String currentSeason = SeasonUtils.getCurrentSeason();

        // 获取所有菜品信息
        List<Map<String, Object>> allDishes = dishRecommendMapper.findAllDishesWithDetails();

        // 计算每道菜品的匹配度
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> dish : allDishes) {
            double matchScore = calculateMatchScore(dish, mainConstitution, userTags, userProvince, currentSeason);
            dish.put("match_score", Math.round(matchScore * 100) + "%");
            result.add(dish);
        }

        // 按匹配度降序排序
        result.sort((a, b) -> Double.compare(
                Double.parseDouble(b.get("match_score").toString().replace("%", "")),
                Double.parseDouble(a.get("match_score").toString().replace("%", ""))
        ));

        return result;
    }

    // 提取用户收藏菜品的所有标签
    private Set<String> extractTags(List<Map<String, Object>> favoriteTags) {
        Set<String> tags = new HashSet<>();
        for (Map<String, Object> tagMap : favoriteTags) {
            for (Object value : tagMap.values()) {
                if (value != null && !value.toString().isEmpty()) {
                    tags.add(value.toString());
                }
            }
        }
        return tags;
    }

    // 获取默认标签
    private Set<String> getDefaultTags() {
        Set<String> defaultTags = new HashSet<>();
        defaultTags.add("清淡");
        defaultTags.add("营养均衡");
        defaultTags.add("易消化");
        return defaultTags;
    }

    // 计算单个菜品的匹配度
    private double calculateMatchScore(Map<String, Object> dish, String mainConstitution, Set<String> userTags, String userProvince, String currentSeason) {
        double constitutionScore = 0.35;
        double tagSimilarityScore = 0.30;
        double seasonScore = 0.20;
        double regionScore = 0.15;

        // 体质契合度
        String suitableConstitution = (String) dish.get("suitable_constitution");
        if (mainConstitution.equals(suitableConstitution)) {
            constitutionScore *= 1.0;
        } else {
            constitutionScore *= 0.5;
        }

        // 标签相似度
        Set<String> dishTags = extractDishTags(dish);
        double intersection = userTags.stream().filter(dishTags::contains).count();
        double union = userTags.size() + dishTags.size() - intersection;
        tagSimilarityScore *= (union == 0 ? 0 : intersection / union);

        // 季节适配性
        Boolean isSeasonMatch = isDishSuitableForSeason(dish, currentSeason);
        if (isSeasonMatch == null) {
            seasonScore *= 0.0;
        } else {
            seasonScore *= (isSeasonMatch ? 1.0 : 0.5);
        }

        // 地区适配性
        String dishProvince = (String) dish.get("region_suitability");
        if (dishProvince == null || dishProvince.isEmpty()) {
            regionScore *= 1.0;
        } else if ("全国".equals(dishProvince)) {
            regionScore *= 1.0;
        } else if (userProvince != null && userProvince.contains(dishProvince)) {
            regionScore *= 1.0;
        } else {
            regionScore *= 0.5;
        }

        return constitutionScore + tagSimilarityScore + seasonScore + regionScore;
    }

    // 判断菜品是否适合当前季节
    private boolean isDishSuitableForSeason(Map<String, Object> dish, String currentSeason) {
        if (dish == null || currentSeason == null || currentSeason.isEmpty()) {
            return false;
        }

        Object value = switch (currentSeason) {
            case "春季" -> dish.get("season_spring");
            case "夏季" -> dish.get("season_summer");
            case "秋季" -> dish.get("season_autumn");
            case "冬季" -> dish.get("season_winter");
            default -> null;
        };

        if (value instanceof Integer) {
            return Integer.valueOf(1).equals(value);
        }

        return false;
    }

    // 提取菜品的所有标签
    private Set<String> extractDishTags(Map<String, Object> dish) {
        Set<String> tags = new HashSet<>();
        for (int i = 1; i <= 5; i++) {
            String tag = (String) dish.get("effect_tag" + i);
            if (tag != null && !tag.isEmpty()) {
                tags.add(tag);
            }
        }
        return tags;
    }
}
