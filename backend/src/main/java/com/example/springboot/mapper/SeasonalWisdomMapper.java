package com.example.springboot.mapper;

import com.example.springboot.entity.SeasonalWisdom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SeasonalWisdomMapper {
    @Select("SELECT * FROM seasonal_wisdom WHERE tag_name = #{tag_name}")
    List<SeasonalWisdom> selectAllById(String tagName);
}
