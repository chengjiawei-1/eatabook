package com.example.springboot.controller;

import com.example.springboot.service.IngredientBookRelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "IngredientBookRelController", description = "食材-章节关联管理")
@CrossOrigin
@RestController
@RequestMapping("/api/ingredient-book-relations")
public class IngredientBookRelController {

    @Autowired
    private IngredientBookRelService service;

    @Operation(summary = "导入食材-章节关联", description = "Excel需包含：食材名称、所属古籍名称、所属章节标题")
    @PostMapping("/imports")
    public ResponseEntity<?> importRel(@RequestParam("file") MultipartFile file) {
        try {
            service.importRelByExcel(file);
            return ResponseEntity.ok("食材关联导入成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("导入失败：" + e.getMessage());
        }
    }
}
