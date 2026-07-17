package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.AncientBooks;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface AncientbooksMapper extends BaseMapper<AncientBooks> {
    @Insert("insert into ancient_books(book_id,book_name,dynasty,author,book_category,intro,nourish_count,book_cover_url,update_time,status,view_count) " +
            "values(#{bookId},#{bookName},#{dynasty},#{author},#{bookCategory},#{intro},#{nourishCount},#{bookCoverUrl},#{updateTime},#{status},#{viewCount})")
    int insert(AncientBooks ancientBooks);

    @Select("select * from ancient_books")
    List<AncientBooks> findAllAncientBooks();

    @Select("select * from ancient_books where book_category=#{bookCategory}")
    List<AncientBooks> findAncientBooksByCategory(String book_category);

    @Select("select * from ancient_books where book_id=#{bookId}")
    AncientBooks findAncientBooksById(String book_id);

    @Select("select * from ancient_books where book_name=#{bookName}")
    AncientBooks selectByBookName(@Param("bookName") String bookName);

    @Delete("delete from ancient_books where book_id=#{bookId}")
    int deleteAncientBooksById(String book_id);

    @Update("UPDATE ancient_books SET view_count = view_count + 1 WHERE book_id = #{bookId}")
    int updateViewCount(@Param("bookId") String bookId);

    // 根据ID更新古籍状态
    @Update("UPDATE ancient_books SET status = #{status}, update_time = #{updateTime} WHERE book_id = #{bookId}")
    int updateStatus(@Param("bookId") String bookId, @Param("status") Integer status, @Param("updateTime") java.util.Date updateTime);

    @Update("""
            UPDATE ancient_books SET
                book_name = #{ancientBooks.bookName},
                dynasty = #{ancientBooks.dynasty},
                author = #{ancientBooks.author},
                book_category = #{ancientBooks.bookCategory},
                intro = #{ancientBooks.intro},
                nourish_count = #{ancientBooks.nourishCount},
                book_cover_url = #{ancientBooks.bookCoverUrl},
                update_time = #{ancientBooks.updateTime}
            WHERE book_id = #{bookId}
            """)
                int updateAncientBooks(
                        @Param("bookId") String bookId,
                        @Param("ancientBooks") AncientBooks ancientBooks
                );
}

