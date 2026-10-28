package com.coding24h.itnews.mapper;

import com.coding24h.itnews.entity.Category;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CategoryMapper {
    @Select("SELECT * FROM category ORDER BY id")
    List<Category> selectAll();

    @Select("SELECT * FROM category WHERE id = #{id}")
    Category selectById(Long id);

    @Insert("INSERT INTO category(name) VALUES(#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE category SET name=#{name} WHERE id=#{id}")
    int update(Category category);

    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteById(Long id);
}
