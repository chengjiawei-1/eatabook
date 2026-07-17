package com.example.springboot.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot.entity.FoodIngredients;
import com.example.springboot.excel.dto.FoodIngredientExcelDTO;
import com.example.springboot.mapper.FoodIngredientsMapper;
import com.example.springboot.service.FoodIngredientsService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class FoodIngredientsServiceImpl implements FoodIngredientsService {
    @Autowired
    private FoodIngredientsMapper foodIngredientsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importIngredientsByExcel(MultipartFile file) {
        try {
            // 解析 Excel
            List<FoodIngredientExcelDTO> list = EasyExcel.read(file.getInputStream())
                    .head(FoodIngredientExcelDTO.class)
                    .sheet()
                    .doReadSync();

            if (list == null || list.isEmpty()) return;

            for (FoodIngredientExcelDTO dto : list) {
                // 判空
                if (StringUtils.isBlank(dto.getIngredientName())) {
                    continue;
                }

                // 查重
                Long count = foodIngredientsMapper.selectCount(new LambdaQueryWrapper<FoodIngredients>()
                        .eq(FoodIngredients::getIngredientName, dto.getIngredientName()));

                if (count > 0) {
                    log.info("食材[{}]已存在，跳过", dto.getIngredientName());
                    continue;
                }

                // 转换与插入
                FoodIngredients ingredient = new FoodIngredients();
                BeanUtils.copyProperties(dto, ingredient);

                ingredient.setUpdateTime(new Date());

                foodIngredientsMapper.insert(ingredient);
            }
        } catch (IOException e) {
            log.error("食材导入失败", e);
            throw new RuntimeException("Excel读取异常");
        }
    }

    @Override
    public int deleteIngredients(Integer ingredientId){
        return foodIngredientsMapper.deleteIngredientById(ingredientId);
    }

    @Override
    public int updateIngredients(Integer ingredientId, FoodIngredients foodIngredients){
        return foodIngredientsMapper.updateIngredientById(ingredientId,foodIngredients);
    }

    @Override
    public PageInfo<FoodIngredients> findIngredientsByPage(Integer pageNo, Integer pageSize, Integer chapterId) {
        PageHelper.startPage(pageNo, pageSize);
        List<FoodIngredients> list = foodIngredientsMapper.findIngredientsByChapterId(chapterId);
        return new PageInfo<>(list);
    }

    @Override
    public List<FoodIngredients> findIngredientsByChapterId(Integer chapter_id) {
        return foodIngredientsMapper.findIngredientsByChapterId(chapter_id);
    }

    @Override
    public List<FoodIngredients> findIngredientsByDishId(Integer dish_id) {
        return foodIngredientsMapper.findIngredientsByDishId(dish_id);
    }

    @Override
    public List<FoodIngredients>findAll() {
        return  foodIngredientsMapper.findAllIngredients();
    }
}