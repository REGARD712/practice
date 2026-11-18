package com.petadoption.mapper;

import com.petadoption.entity.Breed;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    /**
     * 查询全部品种，并附带每种品种关联的宠物数量。
     * 子查询统计 pet 表中 breed_id 相同的记录，结果映射到 Breed.petCount，仅供列表展示。
     */
    @Select("""
            SELECT b.id, b.name, b.species, b.description,
                   (SELECT COUNT(*) FROM pet p WHERE p.breed_id = b.id) AS pet_count
            FROM breed b
            ORDER BY b.species, b.name, b.id
            """)
    List<Breed> findAllWithPetCount();

    /**
     * 统计同名品种数量，用于新增和改名时的唯一性校验。
     * excludeId 非空时排除自身，避免改名时把当前记录算作重复。
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM breed WHERE name = #{name}
            <if test="excludeId != null"> AND id != #{excludeId} </if>
            </script>
            """)
    long countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    /** 新增品种；species 由表单提供，name 已在服务层完成去空白和唯一性校验。 */
    @Insert("INSERT INTO breed (name, species) VALUES (#{name}, #{species})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Breed breed);

    /** 按主键修改品种名称；名称的唯一性由服务层在同一事务内校验。 */
    @Update("UPDATE breed SET name = #{name} WHERE id = #{id}")
    int updateName(@Param("id") Long id, @Param("name") String name);

    /** 统计引用该品种的宠物数量，作为删除前的引用保护依据。 */
    @Select("SELECT COUNT(*) FROM pet WHERE breed_id = #{breedId}")
    long countPetsByBreedId(Long breedId);

    /** 按主键删除品种；删除前必须确认已无宠物引用。 */
    @Delete("DELETE FROM breed WHERE id = #{id}")
    int deleteById(Long id);
}
