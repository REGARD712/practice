package com.petadoption.mapper;

import com.petadoption.entity.Breed;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 品种表 breed 的 MyBatis Mapper，集中声明品种查询 SQL。
 */
public interface BreedMapper {
    /**
     * 查询全部品种，依次按物种、品种名称和主键升序排列，保证列表顺序稳定。
     * SELECT 显式列出与 Breed 字段对应的列，MyBatis 按列名完成基础映射。
     */
    @Select("SELECT id, name, species, description FROM breed ORDER BY species, name, id")
    List<Breed> findAll();

    /** 按主键查询单个品种；#{id} 是 MyBatis 预编译参数，未找到时返回 null。 */
    @Select("SELECT id, name, species, description FROM breed WHERE id = #{id}")
    Breed findById(Long id);

    /** 统计 breed 表中的品种总数，返回值类型为 long。 */
    @Select("SELECT COUNT(*) FROM breed")
    long count();
}
