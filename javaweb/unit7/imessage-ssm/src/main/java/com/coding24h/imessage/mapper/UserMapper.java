package com.coding24h.imessage.mapper;

import com.coding24h.imessage.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Long id);

    @Select("SELECT * FROM user")
    List<User> selectAll();
}
