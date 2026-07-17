package com.example.springboot.controller;

import com.example.springboot.entity.DishIngredientRel;
import com.example.springboot.service.DishIngredientRelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "DishIngredientRelController", description = "菜品-食材关联管理")
@CrossOrigin
@RestController
@RequestMapping("/api/dish-ingredient-relations")
public class DishIngredientRelController {

    @Autowired
    private DishIngredientRelService service;

    @Operation(summary = "导入菜品-食材关联", description = "Excel需包含：菜品名称、食材名称、用量说明")
    @PostMapping("/imports")
    public ResponseEntity<?> importRel(@RequestParam("file") MultipartFile file) {
        try {
            service.importDishIngredientRel(file);
            return ResponseEntity.ok("菜品-食材关联导入成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("导入失败：" + e.getMessage());
        }
    }

    @Operation(summary = "查询关联", description = "查询所有关联")
    @GetMapping
    public List<DishIngredientRel> list() {
        return service.findall();
    }

    @Operation(summary = "删除关联", description = "根据菜品ID删除关联")
    @DeleteMapping("/{relId}")
    public int deleteByRelId(@PathVariable Integer relId) {
        return service.deleteByRelId(relId);
    }
}
