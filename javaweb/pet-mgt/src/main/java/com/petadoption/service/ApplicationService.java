package com.petadoption.service;

import com.petadoption.entity.AdoptionApplication;
import com.petadoption.entity.Pet;
import com.petadoption.entity.User;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.AdoptionApplicationMapper;
import com.petadoption.mapper.PetMapper;
import com.petadoption.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 领养申请业务服务。
 * 负责申请查询、用户提交申请和管理员审核，并在事务内协调申请状态与宠物状态。
 */
@Service
@RequiredArgsConstructor
public class ApplicationService {
    /** 提供领养申请的查询、新增、行锁读取和条件审核更新。 */
    private final AdoptionApplicationMapper applicationMapper;
    /** 提供宠物查询、条件状态抢占及审核后的状态更新。 */
    private final PetMapper petMapper;
    /** 根据当前登录用户名定位真实申请人。 */
    private final UserMapper userMapper;

    /**
     * 按编号查询单条申请。
     *
     * @param id 申请编号
     * @return 包含关联展示信息的申请记录
     */
    public AdoptionApplication getById(Long id) {
        // 查询申请及其宠物、申请人等关联信息。
        AdoptionApplication application = applicationMapper.findById(id);
        if (application == null) {
            // 对不存在的编号统一按资源不存在处理，而不是返回空对象。
            throw new ResourceNotFoundException("领养申请不存在");
        }
        return application;
    }

    /**
     * 查询指定登录用户本人的申请。
     *
     * @param username 当前认证用户名
     * @return 该用户按时间倒序排列的申请列表
     */
    public List<AdoptionApplication> findMine(String username) {
        // 根据会话用户名解析用户主键，不信任客户端提交的用户编号。
        User user = requireUser(username);
        // 以用户主键限定查询范围，确保只能看到本人数据。
        return applicationMapper.findByUserId(user.getId());
    }

    /**
     * 查询后台申请列表，可选按状态筛选。
     *
     * @param status 申请状态，空白表示查询全部状态
     * @return 符合条件的申请列表
     */
    public List<AdoptionApplication> findAll(String status) {
        // 将纯空白筛选条件归一化为 null，交由动态 SQL 忽略该条件。
        return applicationMapper.findAll(trimToNull(status));
    }

    /**
     * 查询后台仪表盘展示的近期申请。
     *
     * @return 最近的申请列表
     */
    public List<AdoptionApplication> findRecent() {
        return applicationMapper.findRecent();
    }

    /**
     * 提交领养申请，并将宠物从 AVAILABLE 原子更新为 PENDING。
     * 方法处于同一数据库事务中：任一校验或状态更新失败，已插入的申请也会回滚。
     *
     * @param petId 路径指定的宠物编号
     * @param username 当前认证用户名
     * @param form 用户填写的申请资料，身份和审核字段由服务端重新设置
     */
    @Transactional
    public void submit(Long petId, String username, AdoptionApplication form) {
        // 确认当前会话仍对应有效系统用户，并取得申请人主键。
        User user = requireUser(username);
        // 在提交时重新读取宠物，不能仅依赖打开表单时的状态。
        Pet pet = petMapper.findById(petId);
        if (pet == null) {
            throw new ResourceNotFoundException("宠物不存在");
        }
        // 只有可申请状态允许进入后续流程，提前拦截明显的重复或无效申请。
        if (!"AVAILABLE".equals(pet.getStatus())) {
            throw new BusinessException("该宠物当前不可申请领养");
        }

        // 清除客户端可能伪造的主键，确保始终新增申请。
        form.setId(null);
        // 宠物归属以路径参数为准，而不是请求体中的隐藏字段。
        form.setPetId(petId);
        // 申请人归属以当前登录账号为准，防止代他人提交。
        form.setUserId(user.getId());
        // 去除联系电话首尾空白后保存。
        form.setContactPhone(form.getContactPhone().trim());
        // 去除联系地址首尾空白后保存。
        form.setAddress(form.getAddress().trim());
        // 去除住房条件说明首尾空白后保存。
        form.setHousingCondition(form.getHousingCondition().trim());
        // 养宠经验为可选字段，空白内容统一存为 null。
        form.setExperience(trimToNull(form.getExperience()));
        // 去除申请理由首尾空白后保存。
        form.setReason(form.getReason().trim());
        // 初始状态由服务端固定为待审核，忽略客户端提交的状态。
        form.setStatus("PENDING");
        // 新申请不允许携带审核意见。
        form.setReviewComment(null);
        // 新申请尚未审核，因此审核时间必须为空。
        form.setReviewedAt(null);
        // 先插入待审核申请；若后续抢占宠物失败，事务会撤销此插入。
        applicationMapper.insert(form);
        // 通过“仅当仍为 AVAILABLE”条件更新原子抢占申请资格，成功时宠物转为 PENDING。
        // 并发请求即使同时通过前置检查，也只有一个能将状态改为 PENDING，其余影响行数为 0。
        if (petMapper.markPendingIfAvailable(petId) != 1) {
            // 抛出异常使事务回滚，避免失败请求留下孤立申请记录。
            throw new BusinessException("该宠物已被其他申请人抢先申请，请选择其他宠物");
        }
    }

    /**
     * 审核待处理申请，并同步更新对应宠物状态。
     * 通过申请：PENDING → APPROVED，宠物 → ADOPTED；拒绝申请：PENDING → REJECTED，宠物 → AVAILABLE。
     * 行锁与 PENDING 条件更新共同防止并发重复审核，事务保证两张表同时成功或回滚。
     *
     * @param id 待审核申请编号
     * @param status 审核目标状态，仅允许 APPROVED 或 REJECTED
     * @param reviewComment 可选审核意见，最多 1000 个字符
     */
    @Transactional
    public void review(Long id, String status, String reviewComment) {
        // 使用服务端白名单限制目标状态，防止客户端写入任意状态值。
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new BusinessException("审核结果只能是 APPROVED 或 REJECTED");
        }
        // 清理审核意见首尾空白，纯空白按无意见处理。
        String normalizedComment = trimToNull(reviewComment);
        // 对非空意见执行长度限制，避免超出数据库字段或页面展示预期。
        if (normalizedComment != null && normalizedComment.length() > 1000) {
            throw new BusinessException("审核意见不能超过1000个字符");
        }

        // 使用 SELECT ... FOR UPDATE 锁定申请行，使同一申请的并发审核依次执行。
        AdoptionApplication application = applicationMapper.findByIdForUpdate(id);
        if (application == null) {
            throw new ResourceNotFoundException("领养申请不存在");
        }
        // 获得行锁后再次确认仍为待审核，拒绝重复审核及状态回退。
        if (!"PENDING".equals(application.getStatus())) {
            throw new BusinessException("该申请已审核，不能重复操作");
        }
        // SQL 再次以 PENDING 为更新条件，同时写入结果、意见和当前审核时间。
        // 影响行数必须为 1，否则说明状态已变化，应回滚并提示刷新。
        if (applicationMapper.review(id, status, normalizedComment) != 1) {
            throw new BusinessException("申请状态已变化，请刷新后重试");
        }
        // 审核通过后宠物变为已领养；拒绝后恢复可申请，和申请结果在同一事务提交。
        petMapper.updateStatus(application.getPetId(), "APPROVED".equals(status) ? "ADOPTED" : "AVAILABLE");
    }

    /**
     * 解析并校验当前登录用户。
     *
     * @param username 当前认证用户名
     * @return 数据库中的有效用户
     */
    private User requireUser(String username) {
        // 用户名为空时不访问数据库，其余情况按用户名精确查询。
        User user = username == null ? null : userMapper.findByUsername(username);
        if (user == null) {
            // 会话中的账号已被删除或无效时，要求重新认证。
            throw new BusinessException("登录用户不存在，请重新登录");
        }
        return user;
    }

    /**
     * 将可选文本规范化为可存储形式。
     *
     * @param value 原始文本
     * @return 去除首尾空白后的文本；null 或纯空白返回 null
     */
    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
