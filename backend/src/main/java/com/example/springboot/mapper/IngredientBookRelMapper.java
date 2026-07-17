package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.IngredientBookRel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IngredientBookRelMapper extends BaseMapper<IngredientBookRel> {

    //防止同一个食材在同一个章节重复添加
    @Select("SELECT count(*) FROM ingredient_book_rel WHERE ingredient_id = #{ingredientId} AND chapter_id = #{chapterId}")
    int checkExists(@Param("ingredientId") Integer ingredientId, @Param("chapterId") Integer chapterId);

    @Delete("DELETE FROM ingredient_book_rel WHERE ingredient_id = #{ingredientId}")
    int deleteByIngredientId(@Param("ingredientId") Integer ingredientId);
}