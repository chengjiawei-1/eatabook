package com.example.springboot.service.impl;

import com.alibaba.excel.EasyExcel;
import com.example.springboot.entity.Dishes;
import com.example.springboot.excel.dto.DishExcelDTO;
import com.example.springboot.mapper.DishesMapper;
import com.example.springboot.service.DishesService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.lang3.StringUtils; // 建议用 StringUtils

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class DishesServiceImpl implements DishesService {
    @Autowired
    DishesMapper dishesMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importDishesByExcel(MultipartFile file) {
        try {
            //读取 Excel
            List<DishExcelDTO> list = EasyExcel.read(file.getInputStream())
                    .head(DishExcelDTO.class)
                    .sheet()
                    .doReadSync();

            if (list == null || list.isEmpty()) return;

            for (DishExcelDTO dto : list) {
                //判空
                if (StringUtils.isBlank(dto.getDishName())) {
                    continue;
                }

                //防重复
                if (dishesMapper.countByName(dto.getDishName()) > 0) {
                    log.info("菜品[{}]已存在，跳过", dto.getDishName());
                    continue;
                }

                Dishes dish = new Dishes();
                BeanUtils.copyProperties(dto, dish);

                // 默认值处理
                dish.setCollectCount(0);
                dish.setUpdateTime(new Date());

                // 处理季节
                dish.setSeasonSpring(convertYesNo(dto.getSeasonSpringStr()));
                dish.setSeasonSummer(convertYesNo(dto.getSeasonSummerStr()));
                dish.setSeasonAutumn(convertYesNo(dto.getSeasonAutumnStr()));
                dish.setSeasonWinter(convertYesNo(dto.getSeasonWinterStr()));

                // 插入
                dishesMapper.insert(dish);
            }
        } catch (IOException e) {
            log.error("菜品导入失败", e);
            throw new RuntimeException("Excel读取异常");
        }
    }

    // 辅助方法
    private Integer convertYesNo(String val) {
        if (val != null && (val.contains("是") || val.equalsIgnoreCase("yes") || val.equals("1"))) {
            return 1;
        }
        return 0;
    }

    @Override
    public void deleteDishes(Integer dish_id){
        dishesMapper.deleteDishes(dish_id);
    }

    @Override
    public void updateDishes(@PathVariable Integer dish_id, @RequestBody Dishes dishes){
        dishesMapper.updateDishes(dish_id,dishes);
    }

    @Override
    public PageInfo<Dishes> findDishesByPage(Integer pageNo, Integer pageSize, Integer chapterId) {
        PageHelper.startPage(pageNo, pageSize);
        List<Dishes> list = dishesMapper.findDishesByChapterId(chapterId);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Dishes> findFilterDishesByPage(Integer pageNo, Integer pageSize,String kind, String season) {
        PageHelper.startPage(pageNo, pageSize);
        List<Dishes> list =dishesMapper.filterDishes(kind, season);
        return new PageInfo<>(list);
    }
    @Override
    public PageInfo<Dishes> searchDishes(String keyword, String kind, String season, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<Dishes> list = dishesMapper.searchDishes(keyword, kind, season);
        return new PageInfo<>(list);
    }
    @Override
    public List<Dishes> findAllDishes() { return dishesMapper.findAllDishes(); }
    @Override
    public List<Dishes> findDishesByChapterId(Integer chapter_id) { return dishesMapper.findDishesByChapterId(chapter_id); }
    @Override
    public Dishes findDishesByDishesId(Integer dish_id) { return dishesMapper.findDishesByDishesId(dish_id); }
    @Override
    public List<Dishes> filterDishes(String kind, String season) { return dishesMapper.filterDishes(kind, season); }
}