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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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

    /** 单只宠物相册允许的最大图片数量，与页面提示保持一致。 */
    private static final int MAX_IMAGES = 5;

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
        // 新建宠物固定从“待领养”开始，不采用表单可能携带的状态值，确保状态只由审核流程流转。
        pet.setStatus("AVAILABLE");
        // 在写文件和数据库前确认品种有效，避免无效外键及不必要 I/O。
        validateBreed(pet.getBreedId());
        // 服务端强制相册上限，防止绕过前端一次上传超过 5 张。
        if (countEffectiveImages(imageFiles) > MAX_IMAGES) {
            throw new BusinessException("一次最多上传5张图片");
        }
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
     * 已有图片保持不变；仅当前没有封面时，第一张新图片会成为封面，已有封面不会被新图覆盖。
     * 已有图片与本次新增图片合计不能超过 5 张。
     * 领养状态不随该更新变化，只能由领养申请审核流程修改。
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
        // 已有图片与本次新增图片合计不能超过相册上限。
        long existingImageCount = existing.getImages() == null ? 0 : existing.getImages().size();
        long newImageCount = countEffectiveImages(imageFiles);
        if (existingImageCount + newImageCount > MAX_IMAGES) {
            throw new BusinessException("宠物相册最多5张图片，当前已有" + existingImageCount
                    + "张，本次选择" + newImageCount + "张，请先删除部分图片");
        }
        // 更新时图片非必填；未选择有效图片会得到空路径列表。
        List<String> storedPaths = fileStorageService.storeImages(imageFiles, false);
        try {
            // 更新宠物主表字段，不直接修改原有图片记录。
            petMapper.update(pet);
            // 只有当前完全没有封面时，才让本批第一张成为封面；已有封面不会被新图替换。
            boolean hasExistingCover = existing.getImages() != null && existing.getImages().stream()
                    .anyMatch(image -> Boolean.TRUE.equals(image.getCover()));
            // 逐张追加图片，封面规则与页面提示保持一致。
            for (int i = 0; i < storedPaths.size(); i++) {
                insertImage(pet.getId(), storedPaths.get(i), !hasExistingCover && i == 0);
            }
        } catch (RuntimeException ex) {
            // 回滚数据库更新的同时，仅清理本次新增文件，不触碰已有图片。
            storedPaths.forEach(fileStorageService::deleteUploadedImage);
            throw ex;
        }
    }

    /**
     * 删除没有领养申请的宠物及其图片。
     * 图片关联和宠物主记录在同一事务中删除，物理文件在事务提交成功后清理。
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
        // 文件系统不参与事务：登记提交后回调，只有数据库真正提交成功才删除物理图片。
        // 否则事务回滚后记录仍在，图片却已被永久删除。
        if (pet.getImages() != null && !pet.getImages().isEmpty()) {
            // 先固定待删除路径，回调阶段不再访问可能已关闭的会话或实体。
            List<String> imagePaths = pet.getImages().stream().map(PetImage::getImagePath).toList();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 逐个删除物理文件；单文件失败只记录警告，不影响已提交的删除结果。
                    imagePaths.forEach(fileStorageService::deleteUploadedImage);
                }
            });
        }
    }

    /**
     * 将指定图片设为所属宠物的封面。
     * 在同一事务内先清除该宠物全部封面标记，再设置目标图片，保证任意时刻至多一张封面。
     *
     * @param petId 宠物编号
     * @param imageId 图片编号
     */
    @Transactional
    public void setCoverImage(Long petId, Long imageId) {
        // 确认宠物存在，同时让无效宠物编号统一返回 404。
        getById(petId);
        // 图片必须存在且确实属于该宠物，防止跨宠物篡改封面。
        PetImage image = petImageMapper.findById(imageId);
        if (image == null || !petId.equals(image.getPetId())) {
            throw new BusinessException("图片不存在或不属于该宠物，请刷新后重试");
        }
        // 已经是封面时直接返回，避免无意义的数据库写入。
        if (Boolean.TRUE.equals(image.getCover())) {
            return;
        }
        // 先清除旧封面，再设置新封面；两条语句处于同一事务。
        petImageMapper.clearCover(petId);
        if (petImageMapper.setCover(petId, imageId) != 1) {
            throw new BusinessException("封面更新未生效，请刷新后重试");
        }
    }

    /**
     * 删除宠物相册中的一张图片。
     * 规则：相册至少保留一张照片；删除封面时自动把最早的一张剩余图片设为新封面。
     * 数据库记录在事务内删除，物理文件在提交成功后清理。
     *
     * @param petId 宠物编号
     * @param imageId 图片编号
     */
    @Transactional
    public void deleteImage(Long petId, Long imageId) {
        // 确认宠物存在。
        getById(petId);
        // 图片必须存在且属于该宠物。
        PetImage image = petImageMapper.findById(imageId);
        if (image == null || !petId.equals(image.getPetId())) {
            throw new BusinessException("图片不存在或不属于该宠物，请刷新后重试");
        }
        // 保留最后一张照片，避免宠物相册为空、封面悬空。
        if (petImageMapper.countByPetId(petId) <= 1) {
            throw new BusinessException("宠物至少需要保留一张照片，请先上传新照片再删除");
        }
        // 记录是否删除的是封面，用于删除后自动接替。
        boolean wasCover = Boolean.TRUE.equals(image.getCover());
        // 带宠物编号条件删除，防止并发或越权删除其他宠物的图片。
        if (petImageMapper.deleteByIdAndPetId(petId, imageId) != 1) {
            throw new BusinessException("图片已被删除，请刷新后重试");
        }
        // 删除封面后把最早的一张剩余图片设为新封面。
        if (wasCover) {
            PetImage next = petImageMapper.findFirstByPetId(petId);
            if (next != null) {
                petImageMapper.clearCover(petId);
                petImageMapper.setCover(petId, next.getId());
            }
        }
        // 文件系统不参与事务：提交成功后再删除物理文件，回滚时文件保持不变。
        String imagePath = image.getImagePath();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                fileStorageService.deleteUploadedImage(imagePath);
            }
        });
    }

    /**
     * 统计请求中真正非空的图片数量，忽略表单提交的空文件占位项。
     *
     * @param imageFiles 请求文件列表，可能为 null 或包含空项
     * @return 有效图片数量
     */
    private long countEffectiveImages(List<MultipartFile> imageFiles) {
        return imageFiles == null ? 0
                : imageFiles.stream().filter(file -> file != null && !file.isEmpty()).count();
    }

    /**
     * 将已领养宠物重新开放为待领养。
     * 这是审核流程之外唯一允许的手动状态操作，只允许 ADOPTED → AVAILABLE。
     * 在同一事务内先确认没有待审核申请，再用带原状态条件的原子更新落库，
     * 避免与审核流程并发时覆盖对方的结果。
     *
     * @param id 待重新开放的宠物编号
     * @return 宠物名称，供操作成功提示使用
     */
    @Transactional
    public String reopen(Long id) {
        // 加载宠物，同时验证目标存在并读取当前状态用于准确提示。
        Pet pet = getById(id);
        // 只允许从“已领养”回退，其余状态一律拒绝并说明原因。
        if (!"ADOPTED".equals(pet.getStatus())) {
            throw new BusinessException("只有已领养的宠物才能重新开放，当前状态为「" + statusLabel(pet.getStatus()) + "」");
        }
        // 仍有待审核申请时不能开放，否则同一宠物会同时存在互相冲突的领养流程。
        if (applicationMapper.countByPetIdAndStatus(id, "PENDING") > 0) {
            throw new BusinessException("该宠物仍有待审核的领养申请，不能重新开放");
        }
        // WHERE 中限定原状态为 ADOPTED：并发操作或状态已变化时返回 0，不覆盖其他流程的结果。
        if (petMapper.updateStatus(id, "ADOPTED", "AVAILABLE") != 1) {
            throw new BusinessException("宠物状态已变化，重新开放未生效，请刷新后重试");
        }
        // 返回名称用于操作成功提示，避免控制器在事务提交后再次查询。
        return pet.getName();
    }

    /**
     * 将状态编码转换为中文名称，用于业务提示。
     *
     * @param status AVAILABLE、PENDING 或 ADOPTED
     * @return 对应的中文名称；未知取值原样返回
     */
    private String statusLabel(String status) {
        return switch (status) {
            case "AVAILABLE" -> "待领养";
            case "PENDING" -> "审核中";
            case "ADOPTED" -> "已领养";
            default -> status;
        };
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
     * 去除首尾空白后转义 LIKE 通配符，使 %、_ 和 \ 按普通字符匹配。
     *
     * @param keyword 原始关键词
     * @return null 保持为 null，其余值去除首尾空白并转义通配符
     */
    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        // 反斜杠必须先转义，否则会把后续替换产生的转义符再次处理。
        // 转义后的模式与 PetMapper 中 LIKE ... ESCAPE '\\' 配合生效。
        return keyword.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
