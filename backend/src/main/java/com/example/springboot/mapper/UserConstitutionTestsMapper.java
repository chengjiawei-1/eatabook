package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.UserConstitutionTests;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserConstitutionTestsMapper extends BaseMapper<UserConstitutionTests> {
    // 查询历史记录
    @Select("SELECT * FROM user_constitution_tests WHERE user_id = #{userId} ORDER BY test_time DESC")
    List<UserConstitutionTests> selectHistoryByUserId(Integer userId);

    @Select("SELECT * FROM user_constitution_tests WHERE user_id = #{userId} ORDER BY test_time DESC LIMIT 1")
    UserConstitutionTests selectLatestByUserId(Integer userId);

    @Select("SELECT * FROM user_constitution_tests WHERE user_id = #{userId} ORDER BY test_time ASC")
    List<UserConstitutionTests> selectByUserIdForArgent(Integer userId);

    @Select("SELECT COUNT(*) as totalTests FROM user_constitution_tests")
    Integer totalTests();

    @Select("SELECT COUNT(*) as todayTests FROM user_constitution_tests WHERE DATE(test_time) = CURDATE()")
    Integer todayTests();

    @Select("SELECT COUNT(*) as yesterdayTests \n" +
            "FROM user_constitution_tests \n" +
            "WHERE DATE(test_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Integer yesterdayTests();
}
