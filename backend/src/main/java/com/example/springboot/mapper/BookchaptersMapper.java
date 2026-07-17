package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.BookChapters;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface BookchaptersMapper extends BaseMapper<BookChapters> {
    @Select("select * from book_chapters")
    List<BookChapters> findAllBookChapters();

    @Select("select * from book_chapters where book_id=#{book_id}")
    List<BookChapters> findAncientBooksByCategory(String book_id);

    @Delete("delete from book_chapters where book_id=#{book_id}")
    int deleteByBookId(String book_id);
}