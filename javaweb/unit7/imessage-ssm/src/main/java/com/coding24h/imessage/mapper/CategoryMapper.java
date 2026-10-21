package com.coding24h.imessage.mapper;

import com.coding24h.imessage.entity.Category;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CategoryMapper {
    @Select("SELECT * FROM category WHERE id = #{id}")
    Category selectById(Long id);

    @Select("SELECT * FROM category")
    List<Category> selectAll();
}
