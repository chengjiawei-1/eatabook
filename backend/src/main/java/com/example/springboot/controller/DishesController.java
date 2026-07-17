package com.example.springboot.controller;

import com.example.springboot.entity.Dishes;
import com.example.springboot.service.DishBookRelService;
import com.example.springboot.service.DishIngredientRelService;
import com.example.springboot.service.DishesService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name="DishesController",description="菜品数据库相关接口")
@CrossOrigin
@RestController
@RequestMapping("/api/dishes")
public class DishesController {

    @Autowired
    private DishesService dishesService;
    @Autowired
    private DishBookRelService dishBookRelService;
    @Autowired
    private DishIngredientRelService dishIngredientRelService;

    @Operation(summary = "导入菜品Excel", description = "Excel需包含：菜品名称、适合春季(是/否)、功效等")
    @PostMapping("/imports")
    public ResponseEntity<?> importDishes(@RequestParam("file") MultipartFile file) {
        try {
            dishesService.importDishesByExcel(file);
            return ResponseEntity.ok("菜品导入成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("导入失败：" + e.getMessage());
        }
    }

    @Operation(summary = "单个菜品删除", description = "根据id删除特定菜品")
    @DeleteMapping("/{dishId}")
    public String deleteDishes(@PathVariable Integer dishId) {
        dishIngredientRelService.deleteByDishId(dishId);
        dishBookRelService.deleteByDishId(dishId);
        dishesService.deleteDishes(dishId);
        return "删除成功";
    }

    @Operation(summary = "所有菜品查询", description = "查询所有菜品")
    @GetMapping
    public List<Dishes> getAllDishes() {
        return dishesService.findAllDishes();
    }

    @Operation(summary = "更新菜品信息")
    @PutMapping("/{dishId}")
    public String updateDishes(@PathVariable Integer dishId, @RequestBody Dishes dishes) {
        dishesService.updateDishes(dishId, dishes);
        return "更新成功";
    }

    @Operation(summary = "章节菜品查询", description = "查询某一章节所有菜品")
    @GetMapping("/chapters/{chapterId}")
    public List<Dishes> findDishesByChapterId(@PathVariable Integer chapterId) {
        return dishesService.findDishesByChapterId(chapterId);
    }

    @Operation(summary = "单一菜品查询", description = "查询单一菜品")
    @GetMapping("/{dishId}")
    public Dishes findDishesById(@PathVariable Integer dishId) {
        return dishesService.findDishesByDishesId(dishId);
    }
    @Operation(summary = "多标签菜品搜索查询", description = "多标签菜品搜索框实现")
    @GetMapping("/search")
    public PageInfo<Dishes> searchDishes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) String season,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "6") int pageSize) {
        return dishesService.searchDishes(keyword, kind, season, pageNo, pageSize);
    }
}
