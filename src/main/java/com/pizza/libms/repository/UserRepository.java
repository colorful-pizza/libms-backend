package com.pizza.libms.repository;

import com.pizza.libms.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserRepository {

    @Select("SELECT id, username, password, full_name AS fullName, role FROM `user` WHERE username = #{username} LIMIT 1")
    User findByUsername(@Param("username") String username);

    @Select("SELECT id, username, password, full_name AS fullName, role FROM `user` WHERE id = #{id} LIMIT 1")
    User findById(@Param("id") Long id);

    @Insert("INSERT INTO `user`(username, password, full_name, role) VALUES(#{username}, #{password}, #{fullName}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE `user` SET full_name = #{fullName}, role = #{role} WHERE id = #{id}")
    int updateBasic(User user);

    @Update("UPDATE `user` SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Delete("DELETE FROM `user` WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select({
        "<script>",
        "SELECT COUNT(1) FROM `user`",
        "<where>",
            "<if test=\"username != null and username != ''\"> AND username LIKE CONCAT('%', #{username}, '%') </if>",
            "<if test=\"role != null and role != ''\"> AND role = #{role} </if>",
        "</where>",
        "</script>"
    })
    long count(@Param("username") String username, @Param("role") String role);

    @Select({
        "<script>",
        "SELECT id, username, password, full_name AS fullName, role FROM `user`",
        "<where>",
            "<if test=\"username != null and username != ''\"> AND username LIKE CONCAT('%', #{username}, '%') </if>",
            "<if test=\"role != null and role != ''\"> AND role = #{role} </if>",
        "</where>",
        "ORDER BY id DESC",
        "LIMIT #{limit} OFFSET #{offset}",
        "</script>"
    })
    java.util.List<User> list(@Param("username") String username, @Param("role") String role,
                  @Param("offset") int offset, @Param("limit") int limit);
}
