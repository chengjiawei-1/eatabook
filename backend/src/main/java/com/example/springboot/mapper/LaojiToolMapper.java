package com.example.springboot.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface LaojiToolMapper {

    @Select("""
            SELECT test_id, test_time, main_constitution, sub_constitution, suggest,
                   plan, region, age_avoid, age_focus, food_staple, food_veg, food_meat, daily
            FROM user_constitution_tests
            WHERE user_id = #{userId}
            ORDER BY test_time DESC
            LIMIT 1
            """)
    Map<String, Object> selectLatestConstitution(@Param("userId") Integer userId);

    @Select("""
            SELECT test_id, test_time, main_constitution, sub_constitution, constitution_convert_scores
            FROM user_constitution_tests
            WHERE user_id = #{userId}
            ORDER BY test_time DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectConstitutionTrend(@Param("userId") Integer userId,
                                                      @Param("limit") int limit);

    @Select("""
            <script>
            SELECT dish_id, dish_name, kind, suitable_constitution, unsuitable_constitution,
                   effect_tag1, effect_tag2, effect_tag3, effect_tag4, effect_tag5, ancient_citation
            FROM dishes
            <where>
              <if test="constitution != null and constitution != ''">
                AND (suitable_constitution LIKE CONCAT('%', #{constitution}, '%')
                     OR effect_tag1 LIKE CONCAT('%', #{constitution}, '%')
                     OR effect_tag2 LIKE CONCAT('%', #{constitution}, '%')
                     OR effect_tag3 LIKE CONCAT('%', #{constitution}, '%')
                     OR effect_tag4 LIKE CONCAT('%', #{constitution}, '%')
                     OR effect_tag5 LIKE CONCAT('%', #{constitution}, '%'))
              </if>
              <if test="keyword != null and keyword != ''">
                AND (dish_name LIKE CONCAT('%', #{keyword}, '%')
                     OR kind LIKE CONCAT('%', #{keyword}, '%')
                     OR ancient_citation LIKE CONCAT('%', #{keyword}, '%'))
              </if>
            </where>
            ORDER BY collect_count DESC, dish_id ASC
            LIMIT #{limit}
            </script>
            """)
    List<Map<String, Object>> selectSuitableDishes(@Param("constitution") String constitution,
                                                   @Param("keyword") String keyword,
                                                   @Param("limit") int limit);

    @Select("""
            SELECT fi.ingredient_id, fi.ingredient_name, fi.alias, fi.property, fi.category,
                   fi.ancient_effect, fi.ancient_origin_text,
                   bc.chapter_id, bc.chapter_title, bc.chapter_seq,
                   ab.book_id, ab.book_name, ab.dynasty, ab.author
            FROM food_ingredients fi
            JOIN ingredient_book_rel ibr ON fi.ingredient_id = ibr.ingredient_id
            JOIN book_chapters bc ON ibr.chapter_id = bc.chapter_id
            LEFT JOIN ancient_books ab ON bc.book_id = ab.book_id
            WHERE fi.ingredient_name LIKE CONCAT('%', #{ingredientName}, '%')
               OR fi.alias LIKE CONCAT('%', #{ingredientName}, '%')
            ORDER BY ab.book_name, bc.chapter_seq
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectIngredientAncientChapters(@Param("ingredientName") String ingredientName,
                                                              @Param("limit") int limit);

    @Select("""
            <script>
            SELECT d.dish_id, d.dish_name, fi.ingredient_id, fi.ingredient_name, fi.alias,
                   fi.property, fi.category, fi.ancient_effect
            FROM dishes d
            JOIN dish_ingredient_rel dir ON d.dish_id = dir.dish_id
            JOIN food_ingredients fi ON dir.ingredient_id = fi.ingredient_id
            WHERE
              <choose>
                <when test="dishId != null">d.dish_id = #{dishId}</when>
                <otherwise>d.dish_name LIKE CONCAT('%', #{dishName}, '%')</otherwise>
              </choose>
            ORDER BY d.dish_name, fi.ingredient_name
            LIMIT #{limit}
            </script>
            """)
    List<Map<String, Object>> selectDishIngredients(@Param("dishId") Integer dishId,
                                                    @Param("dishName") String dishName,
                                                    @Param("limit") int limit);

    @Select("""
            SELECT uc.collection_id, uc.target_type, uc.target_id, uc.collection_time,
                   d.dish_name, d.kind, d.suitable_constitution,
                   fi.ingredient_name, fi.property, fi.category,
                   ab.book_name
            FROM user_collections uc
            LEFT JOIN dishes d ON uc.target_type = 'dish' AND uc.target_id = d.dish_id
            LEFT JOIN food_ingredients fi ON uc.target_type = 'ingredient' AND uc.target_id = fi.ingredient_id
            LEFT JOIN ancient_books ab ON uc.target_type = 'book' AND uc.target_id = ab.book_id
            WHERE uc.user_id = #{userId}
            ORDER BY uc.collection_time DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectUserCollections(@Param("userId") Integer userId,
                                                    @Param("limit") int limit);

    @Select("""
            SELECT native_place, city, birthday, content
            FROM users
            WHERE user_id = #{userId}
            """)
    Map<String, Object> selectUserPreference(@Param("userId") Integer userId);

    @Select("""
            SELECT plan_json, generate_date, base_constitution, update_time
            FROM user_meal_plan
            WHERE user_id = #{userId}
            """)
    Map<String, Object> selectUserMealPlan(@Param("userId") Integer userId);

    @Select("""
            SELECT id, checkin_date, is_rewarded, current_streak, last_checkin_date, created_at
            FROM user_checkins
            WHERE user_id = #{userId}
            ORDER BY checkin_date DESC
            LIMIT 1
            """)
    Map<String, Object> selectLatestCheckin(@Param("userId") Integer userId);

    @Select("""
            SELECT id, checkin_date, is_rewarded, current_streak, last_checkin_date, created_at
            FROM user_checkins
            WHERE user_id = #{userId}
            ORDER BY checkin_date DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectRecentCheckins(@Param("userId") Integer userId,
                                                   @Param("limit") int limit);

    @Insert("""
            INSERT INTO user_meal_plan(user_id, plan_json, generate_date, base_constitution, update_time)
            VALUES(#{userId}, #{planJson}, NOW(), #{constitution}, NOW())
            ON DUPLICATE KEY UPDATE
              plan_json = VALUES(plan_json),
              generate_date = VALUES(generate_date),
              base_constitution = VALUES(base_constitution),
              update_time = NOW()
            """)
    int saveUserMealPlan(@Param("userId") Integer userId,
                         @Param("planJson") String planJson,
                         @Param("constitution") String constitution);
}
