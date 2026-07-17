package com.example.springboot.controller;

import com.example.springboot.entity.AncientBooks;
import com.example.springboot.service.AncientbooksService;
import com.example.springboot.service.BookchaptersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name="AncientbooksController",description="古籍相关接口")
@CrossOrigin
@RestController
@RequestMapping("/api/ancientbooks")
public class AncientbooksController {

    private static final Logger log = LoggerFactory.getLogger(AncientbooksController.class);

    @Autowired
    private AncientbooksService ancientbooksService;
    @Autowired
    private BookchaptersService bookchaptersService;

    @Operation(summary = "后台古籍导入", description = "在后台上传古籍信息")
    @PostMapping("/imports")
    public ResponseEntity<?> importBook(@RequestParam("file") MultipartFile file) {
        try {
            ancientbooksService.importByExcel(file);
            return ResponseEntity.ok().body("古籍导入成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("古籍导入失败：" + e.getMessage());
        }
    }


    @Operation(summary = "发布古籍", description = "发布古籍信息")
    @PutMapping("/{id}/publication")
    public ResponseEntity<?> publishBook(@PathVariable("id") String bookId) {
        try {
            boolean result = ancientbooksService.publishBook(bookId);
            if (result) {
                return ResponseEntity.ok().body("古籍发布成功");
            } else {
                return ResponseEntity.badRequest().body("古籍发布失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "所有古籍查询", description = "查询所有古籍")
    @GetMapping
    public List<AncientBooks> getAncientbooks() {
        return ancientbooksService.findAllAncientBooks();
    }

    @Operation(summary = "古籍分类查询", description = "查询某一类古籍")
    @GetMapping("/categories/{bookCategory}")
    public List<AncientBooks> getAncientBooksByCategory(@PathVariable String bookCategory) {
        return ancientbooksService.findAncientBooksByCategory(bookCategory);
    }

    @Operation(summary = "古籍精准查询", description = "根据id查询特定古籍")
    @GetMapping("/{bookId}")
    public AncientBooks getAncientBooksById(@PathVariable String bookId) {
        return ancientbooksService.findAncientBooksById(bookId);
    }

    @Operation(summary = "古籍删除", description = "根据id删除特定古籍")
    @DeleteMapping("/{bookId}")
    public String deleteAncientBooks(@PathVariable String bookId) {
        bookchaptersService.deleteBookChapter(bookId);
        ancientbooksService.deleteAncientBooks(bookId);
        return "删除成功";
    }

    @Operation (summary = "古籍更新", description = "根据id更新古籍")
    @PutMapping("/{bookId}")
    public String updateAncientBooks(@PathVariable String bookId, @RequestBody AncientBooks ancientBooks) {
        ancientbooksService.updateAncientBooks(bookId, ancientBooks);
        return "更新成功";
    }

    @Operation (summary = "古籍访问量增加", description = "增加古籍访问量")
    @PostMapping("/{bookId}/views")
    public ResponseEntity<Void> increaseViewCount(@PathVariable String bookId) {
        try {
            boolean success = ancientbooksService.increaseViewCount(bookId);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error("增加古籍访问量异常，bookId: {}", bookId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
