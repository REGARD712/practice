package com.petadoption.mapper;

import com.petadoption.entity.AdoptionApplication;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 领养申请表 adoption_application 的 MyBatis Mapper。
 * 除申请记录增查与审核外，查询 SQL 还会联表补充宠物名称、申请人用户名和宠物封面图。
 */
public interface AdoptionApplicationMapper {
    /**
     * 领养申请查询的公共基础 SQL：a.* 提供申请表自身字段，pet 与 user 内连接提供展示名称。
     * cover_image 来自 pet_image 的相关子查询：pi.pet_id = a.pet_id 将图片限定为当前申请的宠物，
     * ORDER BY pi.cover DESC 让封面图优先，pi.id ASC 在优先级相同时选择最早 ID，LIMIT 1 确保只返回一张。
     * 若该宠物没有图片，标量子查询返回 null；内连接则要求关联宠物和用户记录均存在。
     */
    String SELECT_COLUMNS = """
            SELECT a.*, p.name AS pet_name, u.username,
                   (SELECT pi.image_path FROM pet_image pi
                    WHERE pi.pet_id = a.pet_id ORDER BY pi.cover DESC, pi.id ASC LIMIT 1) AS cover_image
            FROM adoption_application a
            INNER JOIN pet p ON p.id = a.pet_id
            INNER JOIN `user` u ON u.id = a.user_id
            """;

    /** 定义可复用的申请结果映射，将表字段、联表字段和封面别名映射到 AdoptionApplication。 */
    @Results(id = "applicationResultMap", value = {
            // 将 id 标识为申请实体主键。
            @Result(id = true, property = "id", column = "id"),
            // 将 pet_id 映射为被申请宠物的 petId。
            @Result(property = "petId", column = "pet_id"),
            // 将 user_id 映射为申请人的 userId。
            @Result(property = "userId", column = "user_id"),
            // 将联表得到的 pet_name 映射为宠物名称。
            @Result(property = "petName", column = "pet_name"),
            // 将联表得到的 username 映射为申请人用户名。
            @Result(property = "username", column = "username"),
            // 将 pet_image 子查询的 cover_image 映射为封面图片路径。
            @Result(property = "coverImage", column = "cover_image"),
            // 将 contact_phone 映射为联系电话。
            @Result(property = "contactPhone", column = "contact_phone"),
            // 居住地址对应同名列。
            @Result(property = "address", column = "address"),
            // 将 housing_condition 映射为住房条件说明。
            @Result(property = "housingCondition", column = "housing_condition"),
            // 养宠经验对应同名列。
            @Result(property = "experience", column = "experience"),
            // 领养理由对应同名列。
            @Result(property = "reason", column = "reason"),
            // 审核状态对应同名列。
            @Result(property = "status", column = "status"),
            // 将 review_comment 映射为审核意见。
            @Result(property = "reviewComment", column = "review_comment"),
            // 将 created_at 映射为申请创建时间。
            @Result(property = "createdAt", column = "created_at"),
            // 将 reviewed_at 映射为审核完成时间。
            @Result(property = "reviewedAt", column = "reviewed_at")
    })
    /** 按申请 ID 查询完整申请及宠物、用户、封面展示信息，未找到时返回 null。 */
    @Select(SELECT_COLUMNS + " WHERE a.id = #{id}")
    AdoptionApplication findById(Long id);

    // 复用申请结果映射；本查询只有申请表字段，联表名称和封面字段不会由这条 SQL 提供。
    @ResultMap("applicationResultMap")
    /**
     * 按 ID 查询申请并追加 FOR UPDATE，对命中的申请行加排他行锁。
     * 该方法应在事务中调用，锁会保持到事务提交或回滚，用于审核流程中阻止其他事务同时修改同一申请。
     */
    @Select("SELECT * FROM adoption_application WHERE id = #{id} FOR UPDATE")
    AdoptionApplication findByIdForUpdate(Long id);

    // 使用公共映射转换申请基础字段和联表展示字段。
    @ResultMap("applicationResultMap")
    /** 查询指定用户的全部申请，按申请时间倒序、ID 倒序排列。 */
    @Select(SELECT_COLUMNS + " WHERE a.user_id = #{userId} ORDER BY a.created_at DESC, a.id DESC")
    List<AdoptionApplication> findByUserId(Long userId);

    // 最近申请同样包含宠物名称、用户名和封面路径。
    @ResultMap("applicationResultMap")
    /** 查询最近提交的 5 条申请，创建时间相同时按 ID 倒序，LIMIT 5 限制返回数量。 */
    @Select(SELECT_COLUMNS + " ORDER BY a.created_at DESC, a.id DESC LIMIT 5")
    List<AdoptionApplication> findRecent();

    // 动态列表查询仍复用统一结果映射。
    @ResultMap("applicationResultMap")
    /**
     * 分页以外的全部申请列表查询，结果按创建时间和 ID 倒序排列。
     * 动态 SQL 的 {@code <if>} 仅在 status 非 null 且 trim 后非空时添加状态精确过滤；
     * {@code <where>} 会根据实际条件生成 WHERE，并处理首个 AND，未传状态时查询全部申请。
     * 联表及 pet_image 封面子查询的含义与 SELECT_COLUMNS 完全相同。
     */
    @Select("""
            <script>
            SELECT a.*, p.name AS pet_name, u.username,
                   (SELECT pi.image_path FROM pet_image pi
                    WHERE pi.pet_id = a.pet_id ORDER BY pi.cover DESC, pi.id ASC LIMIT 1) AS cover_image
            FROM adoption_application a
            INNER JOIN pet p ON p.id = a.pet_id
            INNER JOIN `user` u ON u.id = a.user_id
            <where>
                <if test="status != null and status.trim() != ''">
                    AND a.status = #{status}
                </if>
            </where>
            ORDER BY a.created_at DESC, a.id DESC
            </script>
            """)
    // @Param 将方法参数绑定为动态 SQL 中可识别的 status 名称。
    List<AdoptionApplication> findAll(@Param("status") String status);

    /**
     * 插入新的领养申请，返回受影响行数。
     * status 在 VALUES 中固定为 'PENDING'，确保所有新申请都以待审核状态进入系统。
     */
    @Insert("""
            INSERT INTO adoption_application
                (pet_id, user_id, contact_phone, address, housing_condition, experience, reason, status)
            VALUES
                (#{petId}, #{userId}, #{contactPhone}, #{address}, #{housingCondition}, #{experience}, #{reason}, 'PENDING')
            """)
    // 将数据库生成的申请主键回填到 application.id。
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AdoptionApplication application);

    /**
     * 审核申请并写入审核状态、审核意见和当前数据库时间。
     * WHERE 同时限定 id 与 status = 'PENDING'，使“检查待审核状态并更新”由数据库原子完成：
     * 并发审核同一申请时只有一个事务能将该行从 PENDING 改为新状态，其他更新返回 0，
     * 从而避免重复审核覆盖结果；返回 0 也表示记录不存在或当前已不处于待审核状态。
     */
    @Update("""
            UPDATE adoption_application
            SET status = #{status}, review_comment = #{reviewComment}, reviewed_at = CURRENT_TIMESTAMP
            WHERE id = #{id} AND status = 'PENDING'
            """)
    // 三个 @Param 分别绑定申请 ID、审核结果和审核意见。
    int review(@Param("id") Long id,
               @Param("status") String status,
               @Param("reviewComment") String reviewComment);

    /** 统计指定宠物收到的领养申请总数。 */
    @Select("SELECT COUNT(*) FROM adoption_application WHERE pet_id = #{petId}")
    long countByPetId(Long petId);

    /** 统计指定用户提交的领养申请总数。 */
    @Select("SELECT COUNT(*) FROM adoption_application WHERE user_id = #{userId}")
    long countByUserId(Long userId);

    /** 统计指定宠物处于某状态的申请数量，用于拦截仍有待审核申请的宠物重新开放领养。 */
    @Select("SELECT COUNT(*) FROM adoption_application WHERE pet_id = #{petId} AND status = #{status}")
    long countByPetIdAndStatus(@Param("petId") Long petId, @Param("status") String status);
}
