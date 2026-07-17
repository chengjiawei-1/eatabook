package com.example.springboot.service;

import com.example.springboot.entity.AncientBooks;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AncientbooksService {
    List<AncientBooks> findAllAncientBooks();
    List<AncientBooks> findAncientBooksByCategory(String book_category);
    AncientBooks findAncientBooksById(String book_id);
    void importByExcel(MultipartFile file);
    int deleteAncientBooks(String book_id);
    boolean increaseViewCount(String bookId);
    boolean publishBook(String bookId);
    int updateAncientBooks(String book_id,AncientBooks ancientBooks);
}

