package com.petadoption.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 领养申请实体，主体字段对应 adoption_application 表，并携带宠物、申请人和封面展示信息。
 * Lombok 的 @Data 自动生成字段访问器及常用对象方法。
 */
@Data
public class AdoptionApplication {
    /** 领养申请主键，对应 id 列。 */
    private Long id;

    /** 被申请领养的宠物 ID，对应 pet_id 列并关联 pet.id。 */
    private Long petId;

    /** 提交申请的用户 ID，对应 user_id 列并关联 user.id。 */
    private Long userId;

    /** 宠物名称，由 Mapper 联表读取 pet.name 并以 pet_name 别名映射，不是申请表字段。 */
    private String petName;

    /** 申请人用户名，由 Mapper 联表读取 user.username，不是申请表字段。 */
    private String username;

    /**
     * 宠物封面图片路径，数据来自 pet_image 表而非 adoption_application 表。
     * Mapper 的相关子查询按封面优先、id 升序选择当前申请宠物的一张图片。
     */
    private String coverImage;

    /** 申请人的联系电话，对应 contact_phone 列。 */
    // 联系电话为必填项，不能仅填写空白字符。
    @NotBlank(message = "请输入联系电话")
    // 允许数字、加号、括号、连字符和空白，总长度限制为 5 到 30 个字符。
    @Pattern(regexp = "^[0-9+()\\-\\s]{5,30}$", message = "请输入有效的联系电话")
    private String contactPhone;

    /** 申请人居住地址，对应 address 列。 */
    // 居住地址为必填项。
    @NotBlank(message = "请输入居住地址")
    // 地址最多允许 255 个字符。
    @Size(max = 255, message = "居住地址不能超过255个字符")
    private String address;

    /** 申请人的住房条件说明，对应 housing_condition 列。 */
    // 住房条件为必填项。
    @NotBlank(message = "请说明住房条件")
    // 住房条件说明最多允许 1000 个字符。
    @Size(max = 1000, message = "住房条件不能超过1000个字符")
    private String housingCondition;

    /** 既往养宠经验，对应 experience 列；可以不填写。 */
    // 填写经验时最多允许 1000 个字符。
    @Size(max = 1000, message = "养宠经验不能超过1000个字符")
    private String experience;

    /** 申请领养的理由，对应 reason 列。 */
    // 领养理由为必填项。
    @NotBlank(message = "请填写领养理由")
    // 理由长度须为 20 到 1000 个字符，以便提供有效审核信息。
    @Size(min = 20, max = 1000, message = "领养理由应为20到1000个字符")
    private String reason;

    /** 审核状态，对应 status 列；新申请由 SQL 固定写为 PENDING，审核后保存对应结果。 */
    private String status;

    /** 审核意见，对应 review_comment 列，由审核人员在处理申请时填写。 */
    private String reviewComment;

    /** 申请提交时间，对应 created_at 列。 */
    private LocalDateTime createdAt;

    /** 审核完成时间，对应 reviewed_at 列；未审核时可以为空。 */
    private LocalDateTime reviewedAt;
}
