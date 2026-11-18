package com.petadoption.mapper;

import com.petadoption.entity.PetImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 宠物图片表 pet_image 的 MyBatis Mapper，负责图片查询、新增和按宠物删除。
 */
public interface PetImageMapper {
    /**
     * 查询指定宠物的全部图片，cover DESC 让封面图优先，id ASC 保证其余图片顺序稳定。
     * 该查询也供 PetMapper 的一对多 @Many 映射调用，用于填充 Pet.images。
     */
    @Select("SELECT id, pet_id, image_path, cover FROM pet_image WHERE pet_id = #{petId} ORDER BY cover DESC, id ASC")
    List<PetImage> findByPetId(Long petId);

    /** 向 pet_image 插入一条图片记录，参数取自 PetImage 对象，返回受影响行数。 */
    @Insert("INSERT INTO pet_image (pet_id, image_path, cover) VALUES (#{petId}, #{imagePath}, #{cover})")
    // 读取数据库生成的主键，并回填到传入对象的 id 属性。
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PetImage image);

    /** 删除指定宠物在 pet_image 中的全部图片，返回实际删除的行数。 */
    @Delete("DELETE FROM pet_image WHERE pet_id = #{petId}")
    int deleteByPetId(Long petId);

    /** 按主键查询单张图片，用于校验图片是否属于目标宠物。 */
    @Select("SELECT id, pet_id, image_path, cover FROM pet_image WHERE id = #{id}")
    PetImage findById(Long id);

    /** 统计指定宠物当前的图片总数，用于服务端数量上限校验。 */
    @Select("SELECT COUNT(*) FROM pet_image WHERE pet_id = #{petId}")
    long countByPetId(Long petId);

    /** 清除指定宠物的所有封面标记，保证任意时刻至多一张封面。 */
    @Update("UPDATE pet_image SET cover = 0 WHERE pet_id = #{petId}")
    int clearCover(Long petId);

    /** 将指定宠物的一张图片标记为封面；带 pet_id 条件避免跨宠物改状态。 */
    @Update("UPDATE pet_image SET cover = 1 WHERE id = #{id} AND pet_id = #{petId}")
    int setCover(@Param("petId") Long petId, @Param("id") Long id);

    /** 删除指定宠物的一张图片；带 pet_id 条件避免删除其他宠物的图片。 */
    @Delete("DELETE FROM pet_image WHERE id = #{id} AND pet_id = #{petId}")
    int deleteByIdAndPetId(@Param("petId") Long petId, @Param("id") Long id);

    /** 查询指定宠物最早的一张图片，用于删除封面后的自动接替。 */
    @Select("SELECT id, pet_id, image_path, cover FROM pet_image WHERE pet_id = #{petId} ORDER BY id ASC LIMIT 1")
    PetImage findFirstByPetId(Long petId);
}
