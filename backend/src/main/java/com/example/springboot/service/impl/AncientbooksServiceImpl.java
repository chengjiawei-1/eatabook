package com.example.springboot.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot.entity.AncientBooks;
import com.example.springboot.excel.dto.AncientBookExcelDTO;
import com.example.springboot.mapper.AncientbooksMapper;
import com.example.springboot.service.AncientbooksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AncientbooksServiceImpl implements AncientbooksService {

    private static final Logger log = LoggerFactory.getLogger(AncientbooksServiceImpl.class);

    @Autowired
    private AncientbooksMapper ancientbooksMapper;

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1000); // 用于生成唯一ID

 @Override
@Transactional(rollbackFor = Exception.class)
public void importByExcel(MultipartFile file) {
    try {
        List<AncientBookExcelDTO> excelData = parseExcel(file);
        if (excelData == null || excelData.isEmpty()) {
            log.warn("Excel文件为空或解析失败");
            return;
        }

        for (AncientBookExcelDTO dto : excelData) {
            AncientBooks book = new AncientBooks();
            BeanUtils.copyProperties(dto, book);

            initBook(book);

            if (exists(book.getBookName())) {
                continue;
            }

            ancientbooksMapper.insert(book);
        }
    } catch (IOException e) {
        log.error("导入Excel失败", e);
        throw new RuntimeException("导入Excel失败", e);
    }
}


private List<AncientBookExcelDTO> parseExcel(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
        return null;
    }

    //使用EasyExcel解析文件
    return EasyExcel.read(file.getInputStream())
            .head(AncientBookExcelDTO.class)
            .sheet()
            .doReadSync();
}

    private void initBook(AncientBooks book) {
        book.setStatus(0);
        book.setViewCount(0);
        book.setUpdateTime(new Date());
    }

    private boolean exists(String bookName) {
        return ancientbooksMapper.selectOne(
                new LambdaQueryWrapper<AncientBooks>()
                        .eq(AncientBooks::getBookName, bookName)
        ) != null;
    }

    @Override
    @Transactional
    public boolean publishBook(String bookId) {
        try {
            // 检查古籍是否存在
            AncientBooks book = ancientbooksMapper.findAncientBooksById(bookId);
            if (book == null) {
                throw new RuntimeException("古籍不存在");
            }
            // 更新状态为已发布
            int result = ancientbooksMapper.updateStatus(bookId, 1, new Date());
            return result > 0;
        } catch (Exception e) {
            log.error("发布古籍失败", e);
            throw new RuntimeException("发布古籍失败", e);
        }
    }

    @Override
    @Transactional
    public boolean increaseViewCount(String bookId) {
        try {
            int result = ancientbooksMapper.updateViewCount(bookId);
            if (result > 0) {
                log.info("古籍访问量增加成功，bookId: {}", bookId);
                return true;
            } else {
                log.warn("古籍访问量增加失败，bookId: {}", bookId);
                return false;
            }
        } catch (Exception e) {
            log.error("增加古籍访问量异常，bookId: {}", bookId, e);
            return false;
        }
    }

    @Override
    public List<AncientBooks> findAllAncientBooks() {
        return ancientbooksMapper.findAllAncientBooks();
    }

    @Override
    public List<AncientBooks> findAncientBooksByCategory(String book_category) {
        return ancientbooksMapper.findAncientBooksByCategory(book_category);
    }

    @Override
    public AncientBooks findAncientBooksById(String book_id) {
        return ancientbooksMapper.findAncientBooksById(book_id);
    }

    @Override
    public int deleteAncientBooks(String book_id) {
        return ancientbooksMapper.deleteAncientBooksById(book_id);
    }

    @Override
    public int updateAncientBooks(String book_id,AncientBooks ancientBooks){
    return ancientbooksMapper.updateAncientBooks(book_id,ancientBooks);
 }
}
