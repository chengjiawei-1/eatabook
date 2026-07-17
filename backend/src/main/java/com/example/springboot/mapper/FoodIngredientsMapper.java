package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.FoodIngredients;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FoodIngredientsMapper extends BaseMapper<FoodIngredients> {
    @Select("""
        SELECT
            fi.*     -- 选择食材表全部字段
        FROM
            food_ingredients fi
        INNER JOIN
            ingredient_book_rel ibr ON fi.ingredient_id = ibr.ingredient_id
        WHERE
            ibr.chapter_id = #{chapterId}
        ORDER BY
            fi.ingredient_name
        """)
    List<FoodIngredients> findIngredientsByChapterId(Integer chapter_id);

    @Select("""
        SELECT
            fi.*  -- 获取食材表的所有字段
        FROM
            food_ingredients fi
                INNER JOIN
            dish_ingredient_rel dir ON fi.ingredient_id = dir.ingredient_id
        WHERE
            dir.dish_id = #{dish_id}  -- 替换为具体的菜品ID
        ORDER BY
            fi.ingredient_name
        """)
    List<FoodIngredients> findIngredientsByDishId(Integer dish_id);

    @Select("Select * from food_ingredients")
    List<FoodIngredients> findAllIngredients();

    @Delete("delete from food_ingredients where ingredient_id=#{ingredient_id}")
    int deleteIngredientById(Integer ingredient_id);

    @Update("""
    UPDATE food_ingredients
    SET
        ingredient_name = #{ingredient.ingredientName},
        alias = #{ingredient.alias},
        property = #{ingredient.property},
        category = #{ingredient.category},
        ancient_effect = #{ingredient.ancientEffect},
        ancient_origin_text = #{ingredient.ancientOriginText},
        update_time = #{ingredient.updateTime}
    WHERE ingredient_id = #{ingredientId}
    """)
    int updateIngredientById(
            @Param("ingredientId") Integer ingredientId,
            @Param("ingredient") FoodIngredients ingredient
    );

}
