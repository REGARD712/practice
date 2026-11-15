package com.petadoption.service;

import com.petadoption.entity.Breed;
import com.petadoption.entity.Pet;
import com.petadoption.entity.PetImage;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.AdoptionApplicationMapper;
import com.petadoption.mapper.BreedMapper;
import com.petadoption.mapper.PetImageMapper;
import com.petadoption.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 宠物档案与图片业务服务。
 * 提供公开查询、后台统计及宠物增删改，并协调数据库事务与文件系统补偿清理。
 */
@Service
@RequiredArgsConstructor
public class PetService {
    /** 提供宠物主表查询、统计和增删改操作。 */
    private final PetMapper petMapper;
    /** 提供宠物图片关联记录的新增和删除操作。 */
    private final PetImageMapper petImageMapper;
    /** 提供品种查询、列表及存在性校验。 */
    private final BreedMapper breedMapper;
    /** 用于删除前检查宠物是否已经产生领养申请。 */
    private final AdoptionApplicationMapper applicationMapper;
    /** 负责图片文件校验、保存、访问路径生成和物理删除。 */
    private final FileStorageService fileStorageService;

    /**
     * 按编号读取宠物详情。
     *
     * @param id 宠物编号
     * @return 包含品种和图片信息的宠物对象
     */
    public Pet getById(Long id) {
        // 查询宠物主资料、品种名称以及关联图片。
        Pet pet = petMapper.findById(id);
        if (pet == null) {
            // 对无效编号统一抛出资源不存在异常，避免后续空指针。
            throw new ResourceNotFoundException("宠物不存在");
        }
        return pet;
    }

    /**
     * 查询后台全部宠物。
     *
     * @return 全部宠物列表
     */
    public List<Pet> findAll() {
        return petMapper.findAll();
    }

    /**
     * 查询首页推荐宠物。
     *
     * @return 当前可领养的推荐宠物列表
     */
    public List<Pet> findFeatured() {
        return petMapper.findFeatured();
    }

    /**
     * 按筛选条件和数据库偏移量查询一页宠物。
     *
     * @param keyword 可选关键词
     * @param breedId 可选品种编号
     * @param offset 从零开始的记录偏移量
     * @param limit 本页最大记录数
     * @return 当前页宠物列表
     */
    public List<Pet> findPage(String keyword, Long breedId, int offset, int limit) {
        // 查询与计数采用相同的关键词规范化，保证分页结果口径一致。
        return petMapper.findPage(normalizeKeyword(keyword), breedId, offset, limit);
    }

    /**
     * 统计符合搜索条件的宠物总数。
     *
     * @param keyword 可选关键词
     * @param breedId 可选品种编号
     * @return 匹配记录总数
     */
    public long countSearch(String keyword, Long breedId) {
        return petMapper.countSearch(normalizeKeyword(keyword), breedId);
    }

    /**
     * 统计宠物总数。
     *
     * @return 全部宠物数量
     */
    public long count() {
        return petMapper.count();
    }

    /**
     * 按业务状态统计宠物数量。
     *
     * @param status AVAILABLE、PENDING 或 ADOPTED 等状态值
     * @return 指定状态的宠物数量
     */
    public long countByStatus(String status) {
        return petMapper.countByStatus(status);
    }

    /**
     * 统计已配置的品种数量。
     *
     * @return 品种总数
     */
    public long countBreeds() {
        return breedMapper.count();
    }

    /**
     * 查询表单和筛选器使用的全部品种。
     *
     * @return 品种列表
     */
    public List<Breed> findAllBreeds() {
        return breedMapper.findAll();
    }

    /**
     * 创建宠物档案并保存至少一张图片。
     * 数据库写入处于事务中；文件系统无法随事务回滚，因此数据库异常时必须补偿删除本次文件。
     *
     * @param pet 待创建的宠物资料
     * @param imageFiles 新宠物图片，至少包含一张有效文件
     */
    @Transactional
    public void create(Pet pet, List<MultipartFile> imageFiles) {
        // 在写文件和数据库前确认品种有效，避免无效外键及不必要 I/O。
        validateBreed(pet.getBreedId());
        // 先校验并保存全部图片，true 表示创建时至少一张；返回路径保持上传顺序。
        List<String> storedPaths = fileStorageService.storeImages(imageFiles, true);
        try {
            // 插入宠物主记录，数据库生成的主键会回填到 pet.id。
            petMapper.insert(pet);
            // 按上传顺序建立图片关联，第一张图片标记为封面。
            for (int i = 0; i < storedPaths.size(); i++) {
                insertImage(pet.getId(), storedPaths.get(i), i == 0);
            }
        } catch (RuntimeException ex) {
            // 数据库异常会触发事务回滚，同时删除已落盘图片，避免无数据库记录的孤儿文件。
            storedPaths.forEach(fileStorageService::deleteUploadedImage);
            // 保留原始异常，由上层或全局异常处理器继续处理。
            throw ex;
        }
    }

    /**
     * 更新宠物资料，并可追加新图片。
     * 已有图片保持不变；仅原宠物没有图片时，第一张新图片会成为封面。
     *
     * @param pet 待更新宠物资料，其编号已由控制器按路径设置
     * @param imageFiles 可选的新图片列表
     */
    @Transactional
    public void update(Pet pet, List<MultipartFile> imageFiles) {
        // 先读取现有档案和图片，既验证宠物存在，也为封面判断保留更新前状态。
        Pet existing = getById(pet.getId());
        // 确认更新后的品种仍然有效。
        validateBreed(pet.getBreedId());
        // 更新时图片非必填；未选择有效图片会得到空路径列表。
        List<String> storedPaths = fileStorageService.storeImages(imageFiles, false);
        try {
            // 更新宠物主表字段，不直接修改原有图片记录。
            petMapper.update(pet);
            // 根据更新前的数据判断原宠物是否已有图片。
            boolean hasExistingImages = existing.getImages() != null && !existing.getImages().isEmpty();
            // 逐张追加图片；只有原无图片时，才把本批第一张设为新封面。
            for (int i = 0; i < storedPaths.size(); i++) {
                insertImage(pet.getId(), storedPaths.get(i), !hasExistingImages && i == 0);
            }
        } catch (RuntimeException ex) {
            // 回滚数据库更新的同时，仅清理本次新增文件，不触碰已有图片。
            storedPaths.forEach(fileStorageService::deleteUploadedImage);
            throw ex;
        }
    }

    /**
     * 删除没有领养申请的宠物及其图片。
     * 图片关联和宠物主记录在同一事务中删除，物理文件随后逐项清理。
     *
     * @param id 待删除宠物编号
     */
    @Transactional
    public void delete(Long id) {
        // 加载宠物及图片，同时确认目标存在，并为后续物理清理保留路径。
        Pet pet = getById(id);
        // 只要存在任何历史申请就禁止删除，保留申请与宠物之间的业务追溯关系。
        if (applicationMapper.countByPetId(id) > 0) {
            throw new BusinessException("该宠物已有领养申请，不能删除");
        }
        // 先删除宠物图片关联记录，避免删除主记录后留下无效关联。
        petImageMapper.deleteByPetId(id);
        // 再删除宠物主记录；任一步骤失败时事务回滚。
        petMapper.deleteById(id);
        // 数据库删除语句完成后清理物理图片；单文件删除失败由存储服务记录警告。
        if (pet.getImages() != null) {
            pet.getImages().forEach(image -> fileStorageService.deleteUploadedImage(image.getImagePath()));
        }
    }

    /**
     * 构造并插入一条宠物图片关联记录。
     *
     * @param petId 所属宠物编号
     * @param imagePath 文件存储服务返回的访问路径
     * @param cover 是否将该图片设为封面
     */
    private void insertImage(Long petId, String imagePath, boolean cover) {
        // 创建独立的图片实体，不把文件对象直接写入数据库。
        PetImage image = new PetImage();
        // 关联到刚刚创建或正在更新的宠物。
        image.setPetId(petId);
        // 保存可通过 /uploads/** 访问的相对 URL。
        image.setImagePath(imagePath);
        // 按调用方计算结果设置封面标记。
        image.setCover(cover);
        // 写入图片关联表。
        petImageMapper.insert(image);
    }

    /**
     * 校验请求中的品种编号是否有效。
     *
     * @param breedId 表单提交的品种编号
     */
    private void validateBreed(Long breedId) {
        // 空编号或数据库中不存在的编号均按业务校验失败处理。
        if (breedId == null || breedMapper.findById(breedId) == null) {
            throw new BusinessException("所选品种不存在");
        }
    }

    /**
     * 规范化搜索关键词。
     *
     * @param keyword 原始关键词
     * @return null 保持为 null，其余值去除首尾空白
     */
    private String normalizeKeyword(String keyword) {
        return keyword == null ? null : keyword.trim();
    }
}
