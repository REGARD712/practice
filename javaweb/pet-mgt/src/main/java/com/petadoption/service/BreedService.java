package com.petadoption.service;

import com.petadoption.dto.BreedForm;
import com.petadoption.entity.Breed;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.BreedMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 品种管理业务服务：提供后台列表统计，并负责新增、改名和删除的业务校验。
 */
@Service
public class BreedService {
    /** 品种表 Mapper，集中执行品种相关 SQL。 */
    private final BreedMapper breedMapper;

    /**
     * 通过构造器注入 Mapper，便于测试且避免字段注入。
     *
     * @param breedMapper 品种表 Mapper
     */
    public BreedService(BreedMapper breedMapper) {
        this.breedMapper = breedMapper;
    }

    /**
     * 查询全部品种及关联宠物数量，供后台品种管理列表使用。
     *
     * @return 带 petCount 的品种列表
     */
    public List<Breed> findAllWithPetCount() {
        return breedMapper.findAllWithPetCount();
    }

    /**
     * 新增品种。
     * 名称去空白后做长度与唯一性校验，同名品种会被拒绝。
     *
     * @param form 新增品种表单
     */
    @Transactional
    public void create(BreedForm form) {
        // 统一在服务层做空白与长度校验，避免绕过控制器直接调用时写入非法名称。
        String name = normalizeName(form.getName());
        // 同名品种只允许存在一条，避免前台筛选出现重复项。
        if (breedMapper.countByName(name, null) > 0) {
            throw new BusinessException("品种「" + name + "」已存在，请勿重复添加");
        }
        Breed breed = new Breed();
        breed.setName(name);
        // 物种由表单下拉框提供，去掉首尾空白后写入。
        breed.setSpecies(form.getSpecies() == null ? "" : form.getSpecies().trim());
        breedMapper.insert(breed);
    }

    /**
     * 修改品种名称。
     * 名称空白、超长或与其他品种重名时拒绝；名称未变化时直接返回。
     *
     * @param id 品种编号
     * @param rawName 表单提交的新名称
     */
    @Transactional
    public void rename(Long id, String rawName) {
        // 先确认品种存在，避免改名到已被删除的记录。
        Breed existing = breedMapper.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("品种不存在或已被删除");
        }
        String name = normalizeName(rawName);
        // 名称未变化时不写库，也避免把自己算作重名。
        if (name.equals(existing.getName())) {
            return;
        }
        // 排除自身后仍有同名记录则拒绝。
        if (breedMapper.countByName(name, id) > 0) {
            throw new BusinessException("品种「" + name + "」已存在，请换一个名称");
        }
        breedMapper.updateName(id, name);
    }

    /**
     * 删除品种。
     * 仍有宠物引用时拒绝并说明引用数量；无引用时才执行删除。
     *
     * @param id 品种编号
     */
    @Transactional
    public void delete(Long id) {
        // 品种不存在时给出明确的 404 语义。
        Breed existing = breedMapper.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("品种不存在或已被删除");
        }
        // 引用保护：有宠物使用该品种时拒绝删除，避免宠物外键悬空。
        long petCount = breedMapper.countPetsByBreedId(id);
        if (petCount > 0) {
            throw new BusinessException("该品种下仍有 " + petCount + " 只宠物，无法删除，请先调整这些宠物的品种");
        }
        breedMapper.deleteById(id);
    }

    /**
     * 去除名称首尾空白并校验非空与长度。
     *
     * @param name 原始名称
     * @return 规范化后的名称
     */
    private String normalizeName(String name) {
        // 空白名称直接拒绝，不写入数据库。
        if (name == null || name.isBlank()) {
            throw new BusinessException("品种名称不能为空");
        }
        String trimmed = name.trim();
        // 与 breed.name 列长度保持一致。
        if (trimmed.length() > 80) {
            throw new BusinessException("品种名称不能超过80个字符");
        }
        return trimmed;
    }
}
