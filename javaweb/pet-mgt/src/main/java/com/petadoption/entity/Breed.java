package com.petadoption.entity;

import lombok.Data;

/**
 * 宠物品种实体，对应数据库 breed 表。
 * Lombok 的 @Data 会自动生成 getter、setter、toString、equals 和 hashCode 等方法。
 */
@Data
public class Breed {
    /** 品种主键，对应 id 列。 */
    private Long id;

    /** 品种名称，例如“金毛寻回犬”，对应 name 列。 */
    private String name;

    /** 所属物种类别，例如猫或狗，对应 species 列。 */
    private String species;

    /** 品种特点与补充说明，对应 description 列。 */
    private String description;

    /** 关联宠物数量，只在品种管理列表中通过 pet_count 统计列映射，不写入数据库。 */
    private Long petCount;
}
