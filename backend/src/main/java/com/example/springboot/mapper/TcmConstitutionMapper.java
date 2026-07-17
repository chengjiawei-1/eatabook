// TcmConstitutionMapper.java
package com.example.springboot.mapper;

import com.example.springboot.entity.TcmConstitution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TcmConstitutionMapper {

    @Select("SELECT * FROM tcm_constitution ORDER BY constitution_id")
    List<TcmConstitution> selectAll();

    @Select("SELECT * FROM tcm_constitution WHERE constitution_name = #{name}")
    TcmConstitution selectByName(String name);

    @Select("SELECT * FROM tcm_constitution WHERE constitution_id = #{id}")
    TcmConstitution selectById(Integer id);
}