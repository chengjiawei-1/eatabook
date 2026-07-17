package com.example.springboot.mapper;

import com.example.springboot.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO users(username, native_place,city, password, phone, email, avatar_url, register_time, update_time, role, last_login_time, next_test_time) " +
            "VALUES(#{username}, #{nativePlace},#{city}, #{password}, #{phone}, #{email}, #{avatarUrl}, #{registerTime}, #{updateTime}, #{role}, #{lastLoginTime}, #{nextTestTime})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insert(User user);


    @Select("SELECT * FROM users WHERE user_id = #{id}")
    User selectById(Integer id);

    @Select("SELECT * FROM users")
    List<User> selectAll();

    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(String username);

    @Select("SELECT * FROM users WHERE email = #{email}")
    User selectByEmail(String email);

    @Select("SELECT * FROM users WHERE phone = #{phone}")
    User selectByPhone(String phone);

    @Update("UPDATE users SET username=#{username},native_place=#{nativePlace},city=#{city} ,phone=#{phone}, email=#{email}, " +
            "avatar_url=#{avatarUrl}, update_time=#{updateTime} WHERE user_id=#{userId}")
    int update(User user);

    @Update("UPDATE users SET avatar_url=#{avatarUrl}, update_time=#{updateTime} WHERE user_id=#{userId}")
    int updateAvatar(@Param("userId") Integer userId,
                     @Param("avatarUrl") String avatarUrl,
                     @Param("updateTime") LocalDateTime updateTime);

    @Update("UPDATE users SET password=#{password}, update_time=#{updateTime} WHERE user_id=#{userId}")
    int updatePassword(@Param("userId") Integer id, @Param("password") String password, @Param("updateTime") LocalDateTime updateTime);

    @Update("UPDATE users SET update_time = NOW() WHERE user_id = #{userId}")
    void updateUserTime(Long userId);

    @Delete("DELETE FROM users WHERE user_id = #{id}")
    int delete(Integer id);

    @Update("UPDATE users SET last_login_time = #{lastLoginTime} WHERE user_id = #{userId}")
    int updateLastLoginTime(@Param("userId") Integer userId, @Param("lastLoginTime") LocalDateTime lastLoginTime);

    @Update("UPDATE users SET next_test_time = #{nextTestTime} WHERE user_id = #{userId}")
    int updateNextTestTime(@Param("userId") Integer userId, @Param("nextTestTime") LocalDateTime nextTestTime);

    @Update("UPDATE users SET next_test_time = NULL WHERE user_id = #{userId}")
    int clearReminderTime(@Param("userId") Integer userId);

    //更新积分
    @Update("UPDATE users SET points = #{points} WHERE user_id = #{userId}")
    int updatePoints(@Param("userId") Integer userId, @Param("points") Integer points);

    @Update("UPDATE users SET points = points + #{amount} WHERE user_id = #{userId}")
    int addPoints(@Param("userId") Integer userId, @Param("amount") Integer amount);

    @Update("UPDATE users SET following_count = following_count + #{num} WHERE user_id = #{userId}")
    int updateFollowingCount(@Param("userId") Integer userId, @Param("num") Integer num);

    @Update("UPDATE users SET follower_count = follower_count + #{num} WHERE user_id = #{userId}")
    int updateFollowerCount(@Param("userId") Integer userId, @Param("num") Integer num);

    @Update("UPDATE users SET content = #{content}, update_time = NOW() WHERE user_id = #{userId}")
    int updateContent(@Param("userId") Integer userId, @Param("content") String content);
}
