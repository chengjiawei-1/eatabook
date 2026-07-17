package com.example.springboot.mapper;

import com.example.springboot.entity.Dishes;
import com.example.springboot.entity.User;
import com.example.springboot.entity.UserConstitutionTests;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface DataDashboardMapper {
    //环形图-所有用户体质分布
    @Select("""
    SELECT
        uct.main_constitution AS name,
        COUNT(*) AS value
    FROM user_constitution_tests uct
    INNER JOIN users u ON u.user_id = uct.user_id
    GROUP BY uct.main_constitution
    ORDER BY value DESC
    """)
    List<Map<String, Object>> findAllConstitutionDistribution();

    //环形图-指定地区用户体质分布
    @Select("""
    <script>
    SELECT
        uct.main_constitution AS name,
        COUNT(*) AS value
    FROM user_constitution_tests uct
    INNER JOIN users u ON u.user_id = uct.user_id
    <where>
        1 = 1
        <if test="province != null and province != ''">
            AND u.native_place LIKE CONCAT(#{province}, '%')
        </if>
    </where>
    GROUP BY uct.main_constitution
    ORDER BY value DESC
    </script>
    """)
    List<Map<String, Object>> findConstitutionDistributionByProvince(
            @Param("province") String province
    );

   //排行榜-Top8菜品
   @Select("""
   SELECT
       dish_name AS name,
       collect_count AS value
   FROM dishes
   ORDER BY collect_count DESC
   LIMIT #{limit}
   """)
   List<Map<String, Object>> findTop8Dish(@Param("limit") Integer limit);

    //雷达图-体质-菜品
    @Select("""
<script>
SELECT 
    CONCAT(cons.constitution, '-', d.dish_name) AS subject,
    ROUND(
        (d.collect_count * 0.5) + 
        CASE 
            <if test="season == 'spring'">
                WHEN d.season_spring = 1 THEN 20 
                ELSE 0
            </if>
            <if test="season == 'summer'">
                WHEN d.season_summer = 1 THEN 20 
                ELSE 0
            </if>
            <if test="season == 'autumn'">
                WHEN d.season_autumn = 1 THEN 20 
                ELSE 0
            </if>
            <if test="season == 'winter'">
                WHEN d.season_winter = 1 THEN 20 
                ELSE 0
            </if>
            <if test="season == null or season == ''">
                ELSE 0
            </if>
        END,
        0
    ) AS A
FROM (
    SELECT '平和质' AS constitution UNION ALL
    SELECT '气虚质' UNION ALL SELECT '阳虚质' UNION ALL
    SELECT '阴虚质' UNION ALL SELECT '痰湿质' UNION ALL
    SELECT '湿热质' UNION ALL SELECT '血瘀质' UNION ALL
    SELECT '气郁质' UNION ALL SELECT '特禀质'
) cons
LEFT JOIN dishes d ON d.dish_id = (
    SELECT d2.dish_id
    FROM dishes d2
    WHERE d2.suitable_constitution LIKE CONCAT('%', cons.constitution, '%')
    ORDER BY 
        (d2.collect_count * 0.5) + 
        CASE 
            <if test="season == 'spring'">
                WHEN d2.season_spring = 1 THEN 20 
                ELSE 0
            </if>
            <if test="season == 'summer'">
                WHEN d2.season_summer = 1 THEN 20 
                ELSE 0
            </if>
            <if test="season == 'autumn'">
                WHEN d2.season_autumn = 1 THEN 20 
                ELSE 0
            </if>
            <if test="season == 'winter'">
                WHEN d2.season_winter = 1 THEN 20 
                ELSE 0
            </if>
            <if test="season == null or season == ''">
                ELSE 0
            </if>
        END DESC
    LIMIT 1
)
WHERE d.dish_id IS NOT NULL
ORDER BY FIELD(cons.constitution,
               '平和质', '气虚质', '阳虚质', '阴虚质', '痰湿质',
               '湿热质', '血瘀质', '气郁质', '特禀质')
</script>
""")
    List<Map<String, Object>> findTop5DishesByConstitutionAndSeason(
            @Param("season") String season
    );

    // 获取地图数据（指定地区）
    @Select("""
    <script>
    SELECT 
        u.native_place AS province,
        uct.main_constitution AS name,
        COUNT(*) AS value
    FROM users u
    INNER JOIN user_constitution_tests uct ON u.user_id = uct.user_id
    <where>
        1 = 1
        <if test="region != null and region != '' and region != 'all'">
            AND u.native_place = #{region}
        </if>
    </where>
    GROUP BY u.native_place, uct.main_constitution
    ORDER BY u.native_place, value DESC
    </script>
    """)
    List<Map<String, Object>> findMapDataByRegion(
            @Param("region") String region
    );

    // 获取全国地图数据
    @Select("""
    SELECT 
        name,
        constitution
    FROM (
        SELECT 
            u.native_place AS name,
            uct.main_constitution AS constitution,
            ROW_NUMBER() OVER (PARTITION BY u.native_place ORDER BY COUNT(*) DESC) as rn
        FROM users u
        INNER JOIN user_constitution_tests uct ON u.user_id = uct.user_id
        WHERE u.native_place IS NOT NULL AND u.native_place != ''
        GROUP BY u.native_place, uct.main_constitution
    ) ranked
    WHERE rn = 1
    """)
    List<Map<String, Object>> findAllMapData();

    // 获取卡片数据（总用户数）
    @Select("""
    SELECT COUNT(*) as value
    FROM users
    """)
    Integer findTotalUsers();

    // 获取体质测试数量
    @Select("""
    SELECT COUNT(*) as value
    FROM user_constitution_tests
    """)
    Integer findTotalTests();

    // 获取食疗菜品数量
    @Select("""
    SELECT COUNT(*) as value
    FROM dishes
    """)
    Integer findAllDishes();

    // 获取古籍数量
    @Select("""
    SELECT COUNT(*) as value
    FROM ancient_books
    """)
    Integer findAllBooks();

    //词云图
    //获取今天的搜索记录
    @Select("""
        SELECT search_content
        FROM forum_search_record
        WHERE search_content IS NOT NULL
          AND search_time >= CURDATE()
    """)
    List<String> findTodaySearchContents();

    //向搜索关键词表插入分词后的关键词
    @Insert("""
        INSERT INTO forum_search_keyword
            (keyword, stat_date, search_count, last_search_time, create_time, update_time)
        VALUES
            (#{keyword},DATE(#{time}), 1, #{time}, #{time}, #{time})
        ON DUPLICATE KEY UPDATE 
            search_count = search_count + 1,
            last_search_time = #{time},
            update_time = #{time}
    """)
    int upsertKeyword(@Param("keyword") String keyword,
                      @Param("time") LocalDateTime time);
    //获取搜索当天关键词前20
    @Select("""
        SELECT keyword AS text,
               search_count AS value
        FROM forum_search_keyword
        WHERE stat_date = CURDATE()
          AND DATE(last_search_time) = CURDATE()
        ORDER BY search_count DESC
        LIMIT 20
    """
    )
    List<Map<String, Object>> findTop20Keywords();

}



