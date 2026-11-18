package com.petadoption.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台新增品种表单 DTO，承载品种名称与所属物种。
 * Lombok 的 @Data 自动生成字段访问器，供 Thymeleaf 表单绑定与错误回显。
 */
@Data
public class BreedForm {
    /** 品种名称，同名品种只允许存在一条记录。 */
    @NotBlank(message = "请输入品种名称")
    @Size(max = 80, message = "品种名称不能超过80个字符")
    private String name;

    /** 所属物种类别，例如猫、犬，对应 breed.species 列。 */
    @NotBlank(message = "请选择物种")
    @Size(max = 50, message = "物种名称不能超过50个字符")
    private String species;
}
