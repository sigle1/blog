package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM user WHERE qq_openid = #{qqOpenid}")
    User findByQqOpenid(String qqOpenid);

    @Insert("INSERT INTO user (username, password, role, qq_openid) VALUES (#{username}, #{password}, #{role}, #{qqOpenid})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
}
