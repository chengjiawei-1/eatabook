package com.example.springboot.service;

import com.example.springboot.entity.BookChapters;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookchaptersService {
    List<BookChapters> findAllBookChapters();
    List<BookChapters> findAncientBooksByCategory(String book_id);
    void importChaptersByExcel(MultipartFile file);
    int  deleteBookChapter(String bookId);
}
