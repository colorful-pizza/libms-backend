package com.pizza.libms.repository;

import com.pizza.libms.entity.Book;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookRepository {

    @Select("SELECT id, title, author, isbn, category, total_copies AS totalCopies, available_copies AS availableCopies, published_date AS publishedDate FROM book WHERE id = #{id} LIMIT 1")
    Book findById(@Param("id") Long id);

    @Select("SELECT id, title, author, isbn, category, total_copies AS totalCopies, available_copies AS availableCopies, published_date AS publishedDate FROM book WHERE isbn = #{isbn} LIMIT 1")
    Book findByIsbn(@Param("isbn") String isbn);

    @Insert("INSERT INTO book(title, author, isbn, category, total_copies, available_copies, published_date) VALUES(#{title}, #{author}, #{isbn}, #{category}, #{totalCopies}, #{availableCopies}, #{publishedDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Book book);

    @Update("UPDATE book SET title=#{title}, author=#{author}, isbn=#{isbn}, category=#{category}, total_copies=#{totalCopies}, available_copies=#{availableCopies}, published_date=#{publishedDate} WHERE id=#{id}")
    int update(Book book);

    @Delete("DELETE FROM book WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

        @Select("SELECT COUNT(1) FROM borrow_record WHERE book_id = #{bookId}")
        long countBorrowReferences(@Param("bookId") Long bookId);

    @Select({
            "<script>",
            "SELECT COUNT(1) FROM book",
            "<where>",
            "<if test=\"title != null and title != ''\"> AND title LIKE CONCAT('%', #{title}, '%') </if>",
            "<if test=\"author != null and author != ''\"> AND author LIKE CONCAT('%', #{author}, '%') </if>",
            "<if test=\"isbn != null and isbn != ''\"> AND isbn = #{isbn} </if>",
            "<if test=\"category != null and category != ''\"> AND category = #{category} </if>",
            "</where>",
            "</script>"
    })
    long count(@Param("title") String title, @Param("author") String author,
               @Param("isbn") String isbn, @Param("category") String category);

    @Select({
            "<script>",
            "SELECT id, title, author, isbn, category, total_copies AS totalCopies, available_copies AS availableCopies, published_date AS publishedDate FROM book",
            "<where>",
            "<if test=\"title != null and title != ''\"> AND title LIKE CONCAT('%', #{title}, '%') </if>",
            "<if test=\"author != null and author != ''\"> AND author LIKE CONCAT('%', #{author}, '%') </if>",
            "<if test=\"isbn != null and isbn != ''\"> AND isbn = #{isbn} </if>",
            "<if test=\"category != null and category != ''\"> AND category = #{category} </if>",
            "</where>",
            "ORDER BY id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<Book> list(@Param("title") String title, @Param("author") String author,
                    @Param("isbn") String isbn, @Param("category") String category,
                    @Param("offset") int offset, @Param("limit") int limit);
}
