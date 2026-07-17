package com.example.springboot.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot.entity.AncientBooks;
import com.example.springboot.entity.BookChapters;
import com.example.springboot.entity.FoodIngredients;
import com.example.springboot.entity.IngredientBookRel;
import com.example.springboot.excel.dto.IngredientBookRelExcelDTO;
import com.example.springboot.mapper.*;
import com.example.springboot.service.IngredientBookRelService;
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
public class IngredientBookRelServiceImpl implements IngredientBookRelService {

    @Autowired private IngredientBookRelMapper relMapper;
    @Autowired private FoodIngredientsMapper ingredientsMapper;
    @Autowired private AncientbooksMapper ancientbooksMapper;
    @Autowired private BookchaptersMapper bookChaptersMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importRelByExcel(MultipartFile file) {
        try {
            List<IngredientBookRelExcelDTO> list = EasyExcel.read(file.getInputStream())
                    .head(IngredientBookRelExcelDTO.class)
                    .sheet()
                    .doReadSync();

            if (list == null || list.isEmpty()) return;

            for (IngredientBookRelExcelDTO dto : list) {
                //基础校验
                if (StringUtils.isAnyBlank(dto.getIngredientName(), dto.getBookName())) {
                    continue;
                }

                // 查食材 ID
                FoodIngredients ingredient = ingredientsMapper.selectOne(new LambdaQueryWrapper<FoodIngredients>()
                        .eq(FoodIngredients::getIngredientName, dto.getIngredientName())
                        .last("LIMIT 1"));

                if (ingredient == null) {
                    log.warn("食材[{}]不存在，跳过", dto.getIngredientName());
                    continue;
                }

                // 查古籍 ID
                AncientBooks book = ancientbooksMapper.selectByBookName(dto.getBookName());
                if (book == null) {
                    log.warn("古籍[{}]不存在，跳过", dto.getBookName());
                    continue;
                }

                // 查章节 ID
                Integer chapterId = null;
                if (StringUtils.isNotBlank(dto.getChapterTitle())) {
                    BookChapters chapter = bookChaptersMapper.selectOne(new LambdaQueryWrapper<BookChapters>()
                            .eq(BookChapters::getBookId, book.getBookId())
                            .eq(BookChapters::getChapterTitle, dto.getChapterTitle())
                            .last("LIMIT 1"));

                    if (chapter != null) {
                        chapterId = chapter.getChapterId();
                    } else {
                        log.warn("章节[{}]在书[{}]中未找到，跳过", dto.getChapterTitle(), dto.getBookName());
                        continue;
                    }
                }

                // 防重复
                if (chapterId != null) {
                    if (relMapper.checkExists(ingredient.getIngredientId(), chapterId) > 0) {
                        continue;
                    }
                }

                // 插入
                IngredientBookRel rel = new IngredientBookRel();
                rel.setIngredientId(ingredient.getIngredientId());
                rel.setBookId(book.getBookId());
                rel.setChapterId(chapterId);

                relMapper.insert(rel);
            }
        } catch (IOException e) {
            log.error("导入食材-章节关联失败", e);
            throw new RuntimeException("Excel读取异常");
        }
    }

    @Override
    public int deleteByIngredientId(Integer ingredientId){
        return relMapper.deleteByIngredientId(ingredientId);
    }
}