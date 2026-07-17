package com.example.springboot.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot.entity.AncientBooks;
import com.example.springboot.entity.BookChapters;
import com.example.springboot.excel.dto.BookChapterExcelDTO;
import com.example.springboot.mapper.AncientbooksMapper;
import com.example.springboot.mapper.BookchaptersMapper;
import com.example.springboot.service.BookchaptersService;
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
public class BookchaptersServiceImpl implements BookchaptersService {

    @Autowired
    private BookchaptersMapper bookchaptersMapper;

    //查古籍表，找出书名对应的ID
    @Autowired
    private AncientbooksMapper ancientbooksMapper;

    @Override
    public List<BookChapters> findAllBookChapters() {
        return bookchaptersMapper.findAllBookChapters();
    }

    @Override
    public List<BookChapters> findAncientBooksByCategory(String book_id) {
        return bookchaptersMapper.findAncientBooksByCategory(book_id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importChaptersByExcel(MultipartFile file) {
        try {
            //读取 Excel
            List<BookChapterExcelDTO> list = EasyExcel.read(file.getInputStream())
                    .head(BookChapterExcelDTO.class)
                    .sheet()
                    .doReadSync();

            if (list == null || list.isEmpty()) {
                return;
            }

            for (BookChapterExcelDTO dto : list) {
                //判空
                if (StringUtils.isBlank(dto.getBookName()) || StringUtils.isBlank(dto.getChapterTitle())) {
                    continue;
                }

                //根据Excel里的书名去AncientBooks表查book_id
                AncientBooks book = ancientbooksMapper.selectByBookName(dto.getBookName());

                if (book == null) {
                    log.warn("导入跳过：找不到名为[{}]的古籍，请先导入古籍。", dto.getBookName());
                    continue;
                }

                //防重复：检查该书下是否已有同名章节
                Long count = bookchaptersMapper.selectCount(new LambdaQueryWrapper<BookChapters>()
                        .eq(BookChapters::getBookId, book.getBookId())
                        .eq(BookChapters::getChapterTitle, dto.getChapterTitle()));

                if (count > 0) {
                    continue;
                }

                //插入数据
                BookChapters chapter = new BookChapters();
                BeanUtils.copyProperties(dto, chapter);

                chapter.setBookId(book.getBookId());
                chapter.setUpdateTime(new Date());

                //处理空值默认 0
                if(chapter.getIngredientCount() == null) chapter.setIngredientCount(0);
                if(chapter.getDishCount() == null) chapter.setDishCount(0);

                bookchaptersMapper.insert(chapter);
            }
        } catch (IOException e) {
            log.error("章节导入失败", e);
            throw new RuntimeException("Excel读取异常");
        }
    }

    @Override
    public int  deleteBookChapter(String bookId){
        return bookchaptersMapper.deleteByBookId(bookId);
    }
}