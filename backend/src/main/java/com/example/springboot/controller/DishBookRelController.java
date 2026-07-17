package com.example.springboot.controller;

import com.example.springboot.service.DishBookRelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "DishBookRelController", description = "菜品-章节关联管理")
@CrossOrigin
@RestController
@RequestMapping("/api/dish-book-relations")
public class DishBookRelController {

    @Autowired
    private DishBookRelService dishBookRelService;

    @Operation(summary = "导入菜品-章节关联", description = "Excel需包含：菜品名称、所属古籍名称、所属章节标题")
    @PostMapping("/imports")
    public ResponseEntity<?> importRel(@RequestParam("file") MultipartFile file) {
        try {
            dishBookRelService.importRelByExcel(file);
            return ResponseEntity.ok("关联数据导入成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("导入失败：" + e.getMessage());
        }
    }

}
