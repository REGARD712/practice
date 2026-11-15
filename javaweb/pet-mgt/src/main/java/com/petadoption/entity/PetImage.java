package com.petadoption.entity;

import lombok.Data;

/**
 * 宠物图片实体，对应 pet_image 表；一只宠物可以关联多张图片。
 * Lombok 的 @Data 自动生成字段访问器及常用对象方法。
 */
@Data
public class PetImage {
    /** 图片主键，对应 id 列；查询图片排序时也用它保证顺序稳定。 */
    private Long id;

    /** 所属宠物主键，对应 pet_id 列并关联 pet.id。 */
    private Long petId;

    /** 图片的存储路径或访问路径，对应 image_path 列。 */
    private String imagePath;

    /** 是否为封面图：true 表示封面，查询时封面图片优先于普通图片。 */
    private Boolean cover;
}
