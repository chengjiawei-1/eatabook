package com.example.springboot.mapper;

import com.example.springboot.entity.ForumCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ForumCategoryMapper {

    @Select("SELECT * FROM forum_category WHERE status = 1 ORDER BY sort ASC")
    List<ForumCategory> selectAll();

}
