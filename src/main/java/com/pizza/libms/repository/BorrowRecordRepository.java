package com.pizza.libms.repository;

import com.pizza.libms.entity.BorrowRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BorrowRecordRepository {

    @Select("SELECT id, user_id AS userId, book_id AS bookId, borrow_date AS borrowDate, due_date AS dueDate, return_date AS returnDate, status FROM borrow_record WHERE id = #{id}")
    BorrowRecord findById(@Param("id") Long id);

    @Insert("INSERT INTO borrow_record(user_id, book_id, borrow_date, due_date, status) VALUES(#{userId}, #{bookId}, #{borrowDate}, #{dueDate}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BorrowRecord record);

    @Update("UPDATE borrow_record SET return_date = #{returnDate}, status = #{status} WHERE id = #{id}")
    int updateReturn(@Param("id") Long id, @Param("returnDate") java.time.LocalDate returnDate, @Param("status") String status);

    @Select({
            "<script>",
            "SELECT id, user_id AS userId, book_id AS bookId, borrow_date AS borrowDate, due_date AS dueDate, return_date AS returnDate, status FROM borrow_record",
            "<where>",
            "<if test=\"userId != null\"> AND user_id = #{userId} </if>",
            "<if test=\"bookId != null\"> AND book_id = #{bookId} </if>",
            "<if test=\"status != null and status != ''\"> AND status = #{status} </if>",
            "</where>",
            "ORDER BY id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<BorrowRecord> list(@Param("userId") Long userId, @Param("bookId") Long bookId,
                            @Param("status") String status, @Param("offset") int offset, @Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT id, user_id AS userId, book_id AS bookId, borrow_date AS borrowDate, due_date AS dueDate, return_date AS returnDate, status FROM borrow_record",
            "<where>",
            "<if test=\"userId != null\"> AND user_id = #{userId} </if>",
            "<if test=\"bookId != null\"> AND book_id = #{bookId} </if>",
            "<if test=\"status != null and status != ''\"> AND status = #{status} </if>",
            "</where>",
            "ORDER BY id DESC",
            "</script>"
    })
    List<BorrowRecord> listAll(@Param("userId") Long userId, @Param("bookId") Long bookId,
                               @Param("status") String status);

    @Select({
            "<script>",
            "SELECT COUNT(1) FROM borrow_record",
            "<where>",
            "<if test=\"userId != null\"> AND user_id = #{userId} </if>",
            "<if test=\"bookId != null\"> AND book_id = #{bookId} </if>",
            "<if test=\"status != null and status != ''\"> AND status = #{status} </if>",
            "</where>",
            "</script>"
    })
    long count(@Param("userId") Long userId, @Param("bookId") Long bookId, @Param("status") String status);
}
