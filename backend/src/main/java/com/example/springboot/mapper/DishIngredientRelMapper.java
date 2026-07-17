package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.DishIngredientRel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishIngredientRelMapper extends BaseMapper<DishIngredientRel> {

    //防止同一个菜里重复添加同一个食材
    @Select("SELECT count(*) FROM dish_ingredient_rel WHERE dish_id = #{dishId} AND ingredient_id = #{ingredientId}")
    int checkExists(@Param("dishId") Integer dishId, @Param("ingredientId") Integer ingredientId);

    @Select("SELECT * from dish_ingredient_rel")
    List<DishIngredientRel> findAll();

    @Delete("DELETE FROM dish_ingredient_rel WHERE dish_id = #{dishId}")
    int deleteDishAndIngredientRelByDishId(@Param("dishId") Integer dishId);

    @Delete("DELETE FROM dish_ingredient_rel WHERE ingredient_id = #{ingredientId}")
    int deleteDishAndIngredientRelByIngredientId(@Param("ingredientId") Integer ingredientId);

    @Delete("DELETE FROM dish_ingredient_rel WHERE rel_id = #{relId}")
    int deleteById(@Param("relId")Integer relId);
}