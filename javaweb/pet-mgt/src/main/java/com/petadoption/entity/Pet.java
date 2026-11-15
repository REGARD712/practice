package com.petadoption.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 宠物实体，主体字段对应 pet 表，同时携带品种名称和图片等查询展示数据。
 * Lombok 的 @Data 自动生成字段访问器及常用对象方法。
 */
@Data
public class Pet {
    /** 宠物主键，对应 pet.id。 */
    private Long id;

    /** 宠物名称，对应 name 列。 */
    // 名称不能为 null、空字符串或纯空白字符。
    @NotBlank(message = "请输入宠物名称")
    // 限制名称最多保存 80 个字符。
    @Size(max = 80, message = "宠物名称不能超过80个字符")
    private String name;

    /** 品种外键，对应 breed_id 列并关联 breed.id。 */
    // 新增或修改宠物时必须指定品种。
    @NotNull(message = "请选择品种")
    private Long breedId;

    /**
     * 品种名称，类型是 String，而不是 Breed 对象或品种 ID。
     * 该值由 PetMapper 联表查询 breed.name，并以列别名 breed 映射到此字段。
     */
    private String breed;

    /** 性别描述，对应 gender 列。 */
    // 性别为必填项，不能只填写空白字符。
    @NotBlank(message = "请选择性别")
    // 性别描述最多允许 20 个字符。
    @Size(max = 20, message = "性别描述不能超过20个字符")
    private String gender;

    /** 宠物月龄，对应 age_months 列；使用月作为统一年龄单位。 */
    // 月龄不能为空。
    @NotNull(message = "请输入月龄")
    // 新生儿宠物允许为 0 个月，但不能为负数。
    @Min(value = 0, message = "月龄不能小于0")
    // 月龄上限为 600，避免明显异常的输入。
    @Max(value = 600, message = "月龄不能超过600")
    private Integer ageMonths;

    /** 健康状况说明，对应 health_status 列。 */
    // 健康状况为必填项。
    @NotBlank(message = "请输入健康状况")
    // 健康状况说明最多允许 100 个字符。
    @Size(max = 100, message = "健康状况不能超过100个字符")
    private String healthStatus;

    /** 宠物详细介绍，对应 description 列。 */
    // 介绍不能为 null、空字符串或纯空白字符。
    @NotBlank(message = "请填写宠物介绍")
    // 介绍长度须为 10 到 2000 个字符。
    @Size(min = 10, max = 2000, message = "宠物介绍应为10到2000个字符")
    private String description;

    /** 领养状态：AVAILABLE 可领养、PENDING 审核中、ADOPTED 已领养；默认可领养。 */
    // 状态字段不能为 null、空字符串或纯空白字符。
    @NotBlank(message = "请选择宠物状态")
    // 仅允许上述三种状态，防止写入无法识别的取值。
    @Pattern(regexp = "AVAILABLE|PENDING|ADOPTED", message = "宠物状态无效")
    private String status = "AVAILABLE";

    /** 档案创建时间，对应 created_at 列。 */
    private LocalDateTime createdAt;

    /**
     * 封面图片路径，不是 pet 表的直接字段。
     * PetMapper 从 pet_image 表中按“封面优先、id 升序”选出一条图片记录并映射到此字段。
     */
    private String coverImage;

    /**
     * 宠物的全部图片集合，数据来自 pet_image 表，而不是 pet 表字段。
     * PetMapper 以当前宠物 id 调用 PetImageMapper.findByPetId 填充，封面图片排在最前。
     */
    private List<PetImage> images;
}
