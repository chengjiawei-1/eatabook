package com.example.springboot.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot.entity.DishIngredientRel;
import com.example.springboot.entity.Dishes;
import com.example.springboot.entity.FoodIngredients;
import com.example.springboot.excel.dto.DishIngredientRelExcelDTO;
import com.example.springboot.mapper.DishIngredientRelMapper;
import com.example.springboot.mapper.DishesMapper;
import com.example.springboot.mapper.FoodIngredientsMapper;
import com.example.springboot.service.DishIngredientRelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class DishIngredientRelServiceImpl implements DishIngredientRelService {

    @Autowired private DishIngredientRelMapper relMapper;
    @Autowired private DishesMapper dishesMapper;
    @Autowired private FoodIngredientsMapper ingredientsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importDishIngredientRel(MultipartFile file) {
        try {
            //读取 Excel
            List<DishIngredientRelExcelDTO> list = EasyExcel.read(file.getInputStream())
                    .head(DishIngredientRelExcelDTO.class)
                    .sheet()
                    .doReadSync();

            if (list == null || list.isEmpty()) return;

            for (DishIngredientRelExcelDTO dto : list) {
                //判空
                if (StringUtils.isAnyBlank(dto.getDishName(), dto.getIngredientName())) {
                    continue;
                }

                //查菜品ID
                Dishes dish = dishesMapper.selectOne(new LambdaQueryWrapper<Dishes>()
                        .eq(Dishes::getDishName, dto.getDishName())
                        .last("LIMIT 1"));

                if (dish == null) {
                    log.warn("菜品[{}]不存在，跳过关联", dto.getDishName());
                    continue;
                }

                //查食材ID
                FoodIngredients ingredient = ingredientsMapper.selectOne(new LambdaQueryWrapper<FoodIngredients>()
                        .eq(FoodIngredients::getIngredientName, dto.getIngredientName())
                        .last("LIMIT 1"));

                if (ingredient == null) {
                    log.warn("食材[{}]不存在，跳过关联", dto.getIngredientName());
                    continue;
                }

                //防重复校验
                if (relMapper.checkExists(dish.getDishId(), ingredient.getIngredientId()) > 0) {
                    continue;
                }

                //插入关联
                DishIngredientRel rel = new DishIngredientRel();
                rel.setDishId(dish.getDishId());
                rel.setIngredientId(ingredient.getIngredientId());
                rel.setUsageDetail(dto.getUsageDetail());

                relMapper.insert(rel);
            }

        } catch (IOException e) {
            log.error("导入菜品-食材关联失败", e);
            throw new RuntimeException("Excel读取异常");
        }
    }
    @Override
    public List<DishIngredientRel> findall(){
        return relMapper.findAll();
    }

    @Override
    public int deleteByDishId(Integer dishId) {
        return relMapper.deleteDishAndIngredientRelByDishId(dishId);
    }

    @Override
    public int deleteByIngredientId(Integer ingredientId) {
        return relMapper.deleteDishAndIngredientRelByIngredientId(ingredientId);
    }

    @Override
    public int deleteByRelId(Integer relId){
        return relMapper.deleteById(relId);
    }
}