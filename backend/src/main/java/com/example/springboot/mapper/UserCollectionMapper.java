package com.example.springboot.mapper;

import com.example.springboot.entity.UserCollections;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserCollectionMapper {

    @Insert("INSERT INTO user_collections(user_id, target_type, target_id, collection_time, update_time) " +
            "VALUES(#{userId}, #{targetType}, #{targetId}, #{collectionTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "collectionId")
    int insert(UserCollections userCollection);

    @Delete("DELETE FROM user_collections WHERE user_id = #{userId} AND collection_id = #{collectionId} ")
    int deleteByUserAndTarget(@Param("userId") Integer userId,
                              @Param("collectionId") Integer collectionId);

    @Select("SELECT * FROM user_collections WHERE collection_id = #{collectionId}")
    UserCollections selectById(Integer collectionId);

    @Select("SELECT * FROM user_collections WHERE user_id = #{userId} ORDER BY collection_time DESC")
    List<UserCollections> selectByUserId(Integer userId);

    @Select("SELECT COUNT(*) FROM user_collections WHERE user_id = #{userId} AND target_type = #{targetType}")
    int countByUserAndType(@Param("userId") Integer userId,
                           @Param("targetType") String targetType);

    @Select("SELECT * FROM user_collections WHERE user_id = #{userId} AND target_type = #{targetType} ORDER BY collection_time DESC")
    List<UserCollections> selectByUserAndType(@Param("userId") Integer userId,
                                             @Param("targetType") String targetType);

    @Select("SELECT COUNT(*) FROM user_collections WHERE target_type = #{targetType}")
    int countByType(String targetType);

    @Update("UPDATE user_collections SET update_time = #{updateTime} WHERE collection_id = #{collectionId}")
    int updateTime(@Param("collectionId") Integer collectionId,
                   @Param("updateTime") LocalDateTime updateTime);
}
