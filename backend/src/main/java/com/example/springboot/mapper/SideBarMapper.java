package com.example.springboot.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SideBarMapper {

    //计算所有贴子数
    @Select("SELECT COUNT(*) FROM forum_post WHERE status = 1")
    Integer getTotalPosts();

    //计算所有用户数
    @Select("SELECT COUNT(*) FROM users")
    Integer getTotalUsers();

    //计算所有分类数
    @Select("SELECT COUNT(*) FROM forum_category")
    Integer getTotalCategories();

    //统计各个类别的帖子数
    @Select("""
        SELECT 
            fc.category_id,
            fc.category_name,
            COUNT(fp.post_id) AS post_count
        FROM forum_category fc
        LEFT JOIN forum_post fp ON fc.category_id = fp.category_id AND fp.status = 1
        GROUP BY fc.category_id, fc.category_name
        ORDER BY post_count DESC
        """)
    List<Map<String, Object>> getCategoryCounts();


}
