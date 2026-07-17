package com.example.springboot.controller;

import com.example.springboot.entity.FoodIngredients;
import com.example.springboot.service.DishIngredientRelService;
import com.example.springboot.service.FoodIngredientsService;
import com.example.springboot.service.IngredientBookRelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Tag(name="FoodIngredientsController",description="食材数据库相关接口")
@CrossOrigin
@RestController
@RequestMapping("/api/food-ingredients")
public class FoodIngredientsController {

    @Autowired
    private FoodIngredientsService foodIngredientsService;
    @Autowired
    private DishIngredientRelService dishIngredientRelService;
    @Autowired
    private IngredientBookRelService ingredientBookRelService;

    @Operation(summary = "导入食材Excel", description = "Excel需包含：食材名称、性味、功效等")
    @PostMapping("/imports")
    public ResponseEntity<?> importIngredients(@RequestParam("file") MultipartFile file) {
        try {
            foodIngredientsService.importIngredientsByExcel(file);
            return ResponseEntity.ok("食材导入成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("导入失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除食材", description = "根据id删除特定食材")
    @DeleteMapping("/{ingredientId}")
    public String deleteIngredients(@PathVariable Integer ingredientId) {
        try {
            System.out.println("开始删除食材ID: " + ingredientId);

            int relCount = dishIngredientRelService.deleteByIngredientId(ingredientId);
            System.out.println("删除菜品-食材关联记录数: " + relCount);

            int bookCount = ingredientBookRelService.deleteByIngredientId(ingredientId);
            System.out.println("删除食材-章节关联记录数: " + bookCount);

            foodIngredientsService.deleteIngredients(ingredientId);
            return "食材删除成功";
        } catch (Exception e) {
            System.out.println("删除食材失败: " + e.getMessage());
            e.printStackTrace();
            return "删除失败: " + e.getMessage();
        }
    }

    @Operation(summary = "更新食材", description = "根据id更新特定食材")
    @PutMapping("/{ingredientId}")
    public String updateIngredients(
            @PathVariable Integer ingredientId,
            @RequestBody FoodIngredients foodIngredients
    ) {
        foodIngredientsService.updateIngredients(ingredientId, foodIngredients);
        return "食材更新成功";
    }


    @Operation(summary = "章节所含食材查询", description = "根据章节查询食材")
    @GetMapping("/chapters/{chapterId}")
    public List<FoodIngredients> findIngredientsByChapterId(@PathVariable Integer chapterId) {
        return foodIngredientsService.findIngredientsByChapterId(chapterId);
    }

    @Operation(summary = "菜品所用食材查询", description = "根据菜品id查询食材")
    @GetMapping("/dishes/{dishId}")
    public List<FoodIngredients> findIngredientsByDishId(@PathVariable Integer dishId) {
        return foodIngredientsService.findIngredientsByDishId(dishId);
    }

    @Operation(summary = "查询所有食材", description = "所有食材查询")
    @GetMapping
    public List<FoodIngredients> findAllIngredients() {
        return foodIngredientsService.findAll();
    }
}
