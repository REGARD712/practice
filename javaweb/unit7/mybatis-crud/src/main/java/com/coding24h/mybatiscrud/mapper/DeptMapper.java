package com.coding24h.mybatiscrud.mapper;

import com.coding24h.mybatiscrud.entity.Dept;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface DeptMapper {
    @Select("SELECT * FROM dept WHERE id = #{id}")
    Dept selectById(Long id);

    @Select("SELECT * FROM dept ORDER BY create_time DESC")
    List<Dept> selectAll();

    @Insert("INSERT INTO dept(name, location) VALUES(#{name}, #{location})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Dept dept);

    @Update("UPDATE dept SET name=#{name}, location=#{location} WHERE id=#{id}")
    int update(Dept dept);

    @Delete("DELETE FROM dept WHERE id = #{id}")
    int deleteById(Long id);
}
