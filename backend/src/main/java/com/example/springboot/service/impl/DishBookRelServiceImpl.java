package com.example.springboot.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot.entity.AncientBooks;
import com.example.springboot.entity.BookChapters;
import com.example.springboot.entity.DishBookRel;
import com.example.springboot.entity.Dishes;
import com.example.springboot.excel.dto.DishBookRelExcelDTO;
import com.example.springboot.mapper.AncientbooksMapper;
import com.example.springboot.mapper.BookchaptersMapper;
import com.example.springboot.mapper.DishBookRelMapper;
import com.example.springboot.mapper.DishesMapper;
import com.example.springboot.service.DishBookRelService;
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
public class DishBookRelServiceImpl implements DishBookRelService {

    @Autowired private DishBookRelMapper dishBookRelMapper;
    @Autowired private DishesMapper dishesMapper;
    @Autowired private AncientbooksMapper ancientbooksMapper;
    @Autowired private BookchaptersMapper bookChaptersMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importRelByExcel(MultipartFile file) {
        try {
            List<DishBookRelExcelDTO> list = EasyExcel.read(file.getInputStream())
                    .head(DishBookRelExcelDTO.class)
                    .sheet()
                    .doReadSync();

            if (list == null || list.isEmpty()) return;

            for (DishBookRelExcelDTO dto : list) {
                //判空
                if (StringUtils.isAnyBlank(dto.getDishName(), dto.getBookName())) {
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

                //查古籍ID
                AncientBooks book = ancientbooksMapper.selectByBookName(dto.getBookName());
                if (book == null) {
                    log.warn("古籍[{}]不存在，跳过关联", dto.getBookName());
                    continue;
                }

                //查章节ID
                Integer chapterId = null;
                if (StringUtils.isNotBlank(dto.getChapterTitle())) {
                    BookChapters chapter = bookChaptersMapper.selectOne(new LambdaQueryWrapper<BookChapters>()
                            .eq(BookChapters::getBookId, book.getBookId())
                            .eq(BookChapters::getChapterTitle, dto.getChapterTitle())
                            .last("LIMIT 1"));

                    if (chapter != null) {
                        chapterId = chapter.getChapterId();
                    } else {
                        log.warn("章节[{}]在书[{}]中未找到，将只关联书", dto.getChapterTitle(), dto.getBookName());
                        continue;
                    }
                }

                //防重复
                if (chapterId != null) {
                    int exists = dishBookRelMapper.checkExists(dish.getDishId(), chapterId);
                    if (exists > 0) continue;
                }

                //插入
                DishBookRel rel = new DishBookRel();
                rel.setDishId(dish.getDishId());
                rel.setBookId(book.getBookId());
                rel.setChapterId(chapterId);

                dishBookRelMapper.insert(rel);
            }

        } catch (IOException e) {
            log.error("导入关联失败", e);
            throw new RuntimeException("Excel读取异常");
        }
    }

    @Override
    public boolean deleteByDishId(Integer dishId){
        dishBookRelMapper.deleteByDishIdAndChapterId(dishId);
        return false;
    }
}