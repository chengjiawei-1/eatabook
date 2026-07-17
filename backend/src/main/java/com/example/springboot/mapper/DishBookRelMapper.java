package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.DishBookRel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishBookRelMapper extends BaseMapper<DishBookRel> {

    @Select("SELECT count(*) FROM dish_book_rel WHERE dish_id = #{dishId} AND chapter_id = #{chapterId}")
    int checkExists(@Param("dishId") Integer dishId, @Param("chapterId") Integer chapterId);

    @Delete("DELETE FROM dish_book_rel WHERE dish_id = #{dishId}")
    int deleteByDishIdAndChapterId(@Param("dishId") Integer dishId);
}