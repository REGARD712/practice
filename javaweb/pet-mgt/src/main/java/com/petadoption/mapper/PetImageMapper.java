package com.petadoption.mapper;

import com.petadoption.entity.PetImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

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
}
