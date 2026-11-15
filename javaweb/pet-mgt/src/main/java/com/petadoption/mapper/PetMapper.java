package com.petadoption.mapper;

import com.petadoption.entity.Pet;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 宠物表 pet 的 MyBatis Mapper，提供宠物详情、列表、搜索、维护、状态变更和统计操作。
 * 查询结果还会携带 breed 表的品种名称以及 pet_image 表的封面、图片集合。
 */
public interface PetMapper {
    /**
     * 宠物查询的公共基础 SQL：p.* 提供 pet 表字段，INNER JOIN breed 按 breed_id 取得品种资料。
     * b.name AS breed 是 String 类型的品种名称，对应 Pet.breed，并非 Breed 对象。
     * cover_image 是 pet_image 的相关标量子查询：仅匹配当前宠物（pi.pet_id = p.id），
     * 按 cover DESC 让封面优先，再按 id ASC 保证选择稳定，LIMIT 1 只取一张图片路径；
     * 没有图片时该别名为 null，Pet.images 的全部图片则由下方 @Many 单独查询。
     */
    String SELECT_COLUMNS = """
            SELECT p.*, b.name AS breed,
                   (SELECT pi.image_path FROM pet_image pi
                    WHERE pi.pet_id = p.id ORDER BY pi.cover DESC, pi.id ASC LIMIT 1) AS cover_image
            FROM pet p
            INNER JOIN breed b ON b.id = p.breed_id
            """;

    /** 定义 petResultMap，显式映射宠物字段、品种名称、封面路径和一对多图片集合。 */
    @Results(id = "petResultMap", value = {
            // 将 id 标识为宠物实体的主键。
            @Result(id = true, property = "id", column = "id"),
            // 宠物名称对应 name 列。
            @Result(property = "name", column = "name"),
            // breed_id 映射为品种外键 breedId。
            @Result(property = "breedId", column = "breed_id"),
            // breed 列实际是 b.name 的别名，映射为 String 类型的品种名称。
            @Result(property = "breed", column = "breed"),
            // 性别描述对应 gender 列。
            @Result(property = "gender", column = "gender"),
            // age_months 映射为以月为单位的 ageMonths。
            @Result(property = "ageMonths", column = "age_months"),
            // health_status 映射为健康状况说明。
            @Result(property = "healthStatus", column = "health_status"),
            // 宠物介绍对应 description 列。
            @Result(property = "description", column = "description"),
            // 领养状态对应 status 列。
            @Result(property = "status", column = "status"),
            // created_at 映射为档案创建时间。
            @Result(property = "createdAt", column = "created_at"),
            // cover_image 来自 pet_image 封面子查询，不是 pet 表字段。
            @Result(property = "coverImage", column = "cover_image"),
            // 将当前宠物 id 传给图片查询，用 pet_image 的全部匹配记录填充 images。
            @Result(property = "images", column = "id",
                    many = @Many(select = "com.petadoption.mapper.PetImageMapper.findByPetId"))
    })
    /** 按宠物 ID 查询详情，包含 String 品种名称、封面路径和全部图片；未找到时返回 null。 */
    @Select(SELECT_COLUMNS + " WHERE p.id = #{id}")
    Pet findById(Long id);

    // 复用宠物结果映射，列表中的每只宠物也会按配置加载图片集合。
    @ResultMap("petResultMap")
    /** 查询全部宠物，按创建时间倒序、ID 倒序排列，保证新档案优先且顺序稳定。 */
    @Select(SELECT_COLUMNS + " ORDER BY p.created_at DESC, p.id DESC")
    List<Pet> findAll();

    // 推荐列表复用同一映射，因此同样包含品种、封面与图片数据。
    @ResultMap("petResultMap")
    /** 查询最多 6 只 AVAILABLE 状态宠物作为推荐内容，最新创建的档案优先。 */
    @Select(SELECT_COLUMNS + " WHERE p.status = 'AVAILABLE' ORDER BY p.created_at DESC, p.id DESC LIMIT 6")
    List<Pet> findFeatured();

    // 动态分页查询继续使用 petResultMap 处理字段与图片关联。
    @ResultMap("petResultMap")
    /**
     * 按条件搜索并分页查询宠物。
     * keyword 非 null 且 trim 后非空时，使用 CONCAT 在两侧添加 %，对宠物名称、品种名称、介绍做 OR 模糊匹配；
     * breedId 非 null 时追加品种 ID 精确条件，两个条件同时存在时通过 AND 组合。
     * {@code <where>} 自动处理 WHERE 和首个 AND；LIMIT #{offset}, #{limit} 分别控制跳过条数与本页最大条数。
     * 品种联表和 pet_image 封面子查询与 SELECT_COLUMNS 一致，结果仍按创建时间、ID 倒序。
     */
    @Select("""
            <script>
            SELECT p.*, b.name AS breed,
                   (SELECT pi.image_path FROM pet_image pi
                    WHERE pi.pet_id = p.id ORDER BY pi.cover DESC, pi.id ASC LIMIT 1) AS cover_image
            FROM pet p
            INNER JOIN breed b ON b.id = p.breed_id
            <where>
                <if test="keyword != null and keyword.trim() != ''">
                    AND (p.name LIKE CONCAT('%', #{keyword}, '%')
                         OR b.name LIKE CONCAT('%', #{keyword}, '%')
                         OR p.description LIKE CONCAT('%', #{keyword}, '%'))
                </if>
                <if test="breedId != null">
                    AND p.breed_id = #{breedId}
                </if>
            </where>
            ORDER BY p.created_at DESC, p.id DESC
            LIMIT #{offset}, #{limit}
            </script>
            """)
    // @Param 将四个方法参数分别暴露为 keyword、breedId、offset 和 limit 供 SQL 使用。
    List<Pet> findPage(@Param("keyword") String keyword,
                       @Param("breedId") Long breedId,
                       @Param("offset") int offset,
                       @Param("limit") int limit);

    /**
     * 统计符合搜索条件的宠物总数，供分页计算使用。
     * 关键词非空白时匹配名称、品种或介绍，breedId 非 null 时精确筛选品种；
     * 动态条件与 findPage 保持一致，但不使用排序、LIMIT，也不查询封面或图片集合。
     */
    @Select("""
            <script>
            SELECT COUNT(*)
            FROM pet p
            INNER JOIN breed b ON b.id = p.breed_id
            <where>
                <if test="keyword != null and keyword.trim() != ''">
                    AND (p.name LIKE CONCAT('%', #{keyword}, '%')
                         OR b.name LIKE CONCAT('%', #{keyword}, '%')
                         OR p.description LIKE CONCAT('%', #{keyword}, '%'))
                </if>
                <if test="breedId != null">
                    AND p.breed_id = #{breedId}
                </if>
            </where>
            </script>
            """)
    // 显式绑定两个过滤参数，确保与 findPage 使用相同的参数名称。
    long countSearch(@Param("keyword") String keyword, @Param("breedId") Long breedId);

    /**
     * 插入宠物基础档案，各参数取自 Pet 对象，返回受影响行数。
     * created_at 未显式写入，由数据库默认值负责；图片需通过 PetImageMapper 另行保存。
     */
    @Insert("""
            INSERT INTO pet (name, breed_id, gender, age_months, health_status, description, status)
            VALUES (#{name}, #{breedId}, #{gender}, #{ageMonths}, #{healthStatus}, #{description}, #{status})
            """)
    // 获取数据库生成的宠物主键，并回填到 pet.id，便于后续保存图片等关联数据。
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Pet pet);

    /** 按 id 更新宠物名称、品种、性别、月龄、健康状况、介绍和状态，返回受影响行数。 */
    @Update("""
            UPDATE pet
            SET name = #{name}, breed_id = #{breedId}, gender = #{gender},
                age_months = #{ageMonths}, health_status = #{healthStatus},
                description = #{description}, status = #{status}
            WHERE id = #{id}
            """)
    int update(Pet pet);

    /** 按 id 直接将宠物状态改为指定值，不带原状态限制；两个 @Param 对应 SQL 参数。 */
    @Update("UPDATE pet SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 仅当宠物当前为 AVAILABLE 时才将其原子更新为 PENDING，返回受影响行数。
     * WHERE 中的原状态条件让数据库在一条 UPDATE 内完成“比较并设置”：并发提交领养申请时，
     * 只有一个更新能成功把 AVAILABLE 改为 PENDING，后续更新因状态已变化而返回 0，避免重复占用。
     */
    @Update("UPDATE pet SET status = 'PENDING' WHERE id = #{id} AND status = 'AVAILABLE'")
    int markPendingIfAvailable(Long id);

    /** 按主键删除宠物记录，返回实际删除的行数。 */
    @Delete("DELETE FROM pet WHERE id = #{id}")
    int deleteById(Long id);

    /** 统计 pet 表中的宠物档案总数。 */
    @Select("SELECT COUNT(*) FROM pet")
    long count();

    /** 按 status 精确统计宠物数量，例如分别统计可领养、审核中或已领养数量。 */
    @Select("SELECT COUNT(*) FROM pet WHERE status = #{status}")
    long countByStatus(String status);
}
