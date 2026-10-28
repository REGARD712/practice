package com.coding24h.itnews.mapper;

import com.coding24h.itnews.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user ORDER BY id")
    List<User> selectAll();

    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Long id);
}
