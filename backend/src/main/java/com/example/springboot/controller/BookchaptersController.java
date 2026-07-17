package com.example.springboot.controller;

import com.example.springboot.entity.BookChapters;
import com.example.springboot.service.BookchaptersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name="BookchaptersController",description="章节数据库相关接口")
@CrossOrigin
@RestController
@RequestMapping("/api")
public class BookchaptersController {

    @Autowired
    private BookchaptersService bookchaptersService;

    @Operation(summary = "导入章节Excel", description = "上传Excel文件导入章节，需包含：所属古籍名称、章节标题等")
    @PostMapping("/book-chapters/imports")
    public ResponseEntity<?> importChapters(@RequestParam("file") MultipartFile file) {
        try {
            bookchaptersService.importChaptersByExcel(file);
            return ResponseEntity.ok("章节导入成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("章节导入失败：" + e.getMessage());
        }
    }

    @Operation(summary = "古籍章节查询", description = "查询特定古籍的所有章节")
    @GetMapping("/ancientbooks/{bookId}/book-chapters")
    public List<BookChapters> getBookChaptersByBookId(@PathVariable String bookId) {
        return bookchaptersService.findAncientBooksByCategory(bookId);
    }
}
