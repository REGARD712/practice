package com.petadoption.service;

import com.petadoption.dto.RegistrationForm;
import com.petadoption.dto.UserForm;
import com.petadoption.entity.Role;
import com.petadoption.entity.User;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResourceNotFoundException;
import com.petadoption.mapper.AdoptionApplicationMapper;
import com.petadoption.mapper.RoleMapper;
import com.petadoption.mapper.UserMapper;
import com.petadoption.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/**
 * 用户账号、角色和注册业务服务。
 * 负责密码加密、资料规范化、唯一性保护、角色分配以及有申请用户的删除约束。
 */
@Service
@RequiredArgsConstructor
public class UserService {
    /** 提供用户资料查询、唯一性预检查和增删改操作。 */
    private final UserMapper userMapper;
    /** 提供角色查询及默认普通用户角色定位。 */
    private final RoleMapper roleMapper;
    /** 提供用户与角色关联记录的插入和删除。 */
    private final UserRoleMapper userRoleMapper;
    /** 用于删除用户前检查其是否已提交领养申请。 */
    private final AdoptionApplicationMapper applicationMapper;
    /** 使用 SecurityConfig 中的 BCrypt 实现加密和校验密码。 */
    private final PasswordEncoder passwordEncoder;

    /**
     * 按用户名精确查询用户。
     *
     * @param username 登录用户名
     * @return 匹配用户；不存在时返回 null
     */
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    /**
     * 按编号读取用户详情。
     *
     * @param id 用户编号
     * @return 包含角色信息的用户对象
     */
    public User getById(Long id) {
        // 加载用户及其角色，供编辑或删除前校验使用。
        User user = userMapper.findById(id);
        if (user == null) {
            // 无效编号统一转换为资源不存在异常。
            throw new ResourceNotFoundException("用户不存在");
        }
        return user;
    }

    /**
     * 查询后台全部用户。
     *
     * @return 用户列表
     */
    public List<User> findAll() {
        return userMapper.findAll();
    }

    /**
     * 查询用户表单可分配的全部角色。
     *
     * @return 角色列表
     */
    public List<Role> findAllRoles() {
        return roleMapper.findAll();
    }

    /**
     * 统计系统用户数量。
     *
     * @return 用户总数
     */
    public long count() {
        return userMapper.count();
    }

    /**
     * 构造后台新建用户表单，并尽量默认分配普通用户角色。
     *
     * @return 带默认角色选择的空用户表单
     */
    public UserForm newForm() {
        // 新建表单对象，所有账号资料由管理员填写。
        UserForm form = new UserForm();
        // 查询普通用户角色，作为权限最小化的默认选项。
        Role userRole = roleMapper.findByRoleName("ROLE_USER");
        if (userRole != null) {
            // 角色存在时默认只选中该角色，不默认授予管理员权限。
            form.setRoleIds(List.of(userRole.getId()));
        }
        return form;
    }

    /**
     * 将现有用户转换为后台编辑表单。
     *
     * @param id 用户编号
     * @return 包含资料和角色编号、但不包含原密码的表单对象
     */
    public UserForm getForm(Long id) {
        // 复用详情查询，确保用户存在并加载当前角色。
        User user = getById(id);
        // 新建独立表单对象，避免直接绑定持久化实体及密码摘要。
        UserForm form = new UserForm();
        // 回填用户主键。
        form.setId(user.getId());
        // 回填登录用户名。
        form.setUsername(user.getUsername());
        // 回填邮箱地址。
        form.setEmail(user.getEmail());
        // 回填可选联系电话。
        form.setPhone(user.getPhone());
        // 回填账号启用状态。
        form.setEnabled(user.getEnabled());
        // 将角色对象列表转换为表单提交所需的角色编号列表。
        form.setRoleIds(user.getRoles().stream().map(Role::getId).toList());
        return form;
    }

    /**
     * 处理用户自助注册。
     * 新账号始终启用且仅授予 ROLE_USER，客户端不能通过表单提升自身权限。
     *
     * @param form 已通过基础校验的注册数据
     */
    @Transactional
    public void register(RegistrationForm form) {
        // 去除用户名首尾空白，保持用户名原有大小写。
        String username = form.getUsername().trim();
        // 邮箱去除空白并统一小写，减少大小写差异造成的重复账号。
        String email = normalizeEmail(form.getEmail());
        // 预先检查用户名和邮箱是否被占用；数据库唯一约束仍作为最终并发兜底。
        ensureUnique(username, email, null);
        // 自助注册只能使用预置普通用户角色。
        Role userRole = roleMapper.findByRoleName("ROLE_USER");
        if (userRole == null) {
            // 缺少基础角色时拒绝创建不完整账号。
            throw new BusinessException("用户角色尚未配置，请联系管理员");
        }

        // 创建全新用户实体，不直接复用注册表单。
        User user = new User();
        // 保存规范化用户名。
        user.setUsername(username);
        // 使用 BCrypt 对密码加密，数据库不保存明文。
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        // 保存规范化邮箱。
        user.setEmail(email);
        // 可选电话去除首尾空白，纯空白存为 null。
        user.setPhone(trimToNull(form.getPhone()));
        // 自助注册账号默认立即可登录。
        user.setEnabled(true);
        // 在同一事务中插入用户和唯一的普通角色关联。
        saveNewUser(user, List.of(userRole.getId()));
    }

    /**
     * 由管理员创建用户并分配指定角色。
     *
     * @param form 后台用户表单
     */
    @Transactional
    public void create(UserForm form) {
        // 新建用户必须提供有效密码，并执行最小长度校验。
        validatePassword(form.getPassword(), true);
        // 规范化用户名。
        String username = form.getUsername().trim();
        // 规范化邮箱。
        String email = normalizeEmail(form.getEmail());
        // 在新增前检查用户名和邮箱唯一性。
        ensureUnique(username, email, null);
        // 去重并逐个验证角色，确保至少分配一个真实存在的角色。
        List<Long> roleIds = validateRoles(form.getRoleIds());

        // 创建待持久化用户实体。
        User user = new User();
        // 写入规范化用户名。
        user.setUsername(username);
        // 对管理员设置的初始密码进行 BCrypt 加密。
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        // 写入规范化邮箱。
        user.setEmail(email);
        // 清理可选电话字段。
        user.setPhone(trimToNull(form.getPhone()));
        // 未显式提交启用状态时默认启用，否则采用管理员选择。
        user.setEnabled(form.getEnabled() == null || form.getEnabled());
        // 在事务内保存账号及所有角色关联，任一步骤失败则整体回滚。
        saveNewUser(user, roleIds);
    }

    /**
     * 更新用户资料、可选密码和角色集合。
     * 唯一性预检查排除当前用户，数据库唯一异常用于捕获并发写入冲突。
     *
     * @param form 后台编辑表单，编号已由路径参数确定
     * @param currentUserId 当前登录管理员的主键，用于识别“编辑自己”并施加自保护
     */
    @Transactional
    public void update(UserForm form, Long currentUserId) {
        // 加载原用户并确认其存在，后续在同一对象上组装更新字段。
        User existing = getById(form.getId());
        // 编辑时密码可以留空；只有填写了新密码才校验长度。
        validatePassword(form.getPassword(), false);
        // 规范化新用户名。
        String username = form.getUsername().trim();
        // 规范化新邮箱。
        String email = normalizeEmail(form.getEmail());
        // 检查与其他用户的冲突，当前用户自身沿用原值不算重复。
        ensureUnique(username, email, existing.getId());
        // 去重并验证更新后的角色集合。
        List<Long> roleIds = validateRoles(form.getRoleIds());

        // 计算本次提交是否真的改变了启用状态或角色，未改变的自我编辑（如改邮箱）不受限制。
        boolean newEnabled = form.getEnabled() != null && form.getEnabled();
        boolean enabledChanged = Boolean.TRUE.equals(existing.getEnabled()) != newEnabled;
        List<Long> currentRoleIds = existing.getRoles().stream().map(Role::getId).toList();
        boolean rolesChanged = !new HashSet<>(currentRoleIds).equals(new HashSet<>(roleIds));

        // 自保护：管理员不能停用自己，否则下次登录起将永久失去后台入口。
        if (existing.getId().equals(currentUserId) && enabledChanged) {
            throw new BusinessException("不能修改自己的账号启用状态，避免把自己锁在后台之外，请让其他管理员操作");
        }
        // 自保护：管理员不能调整自己的角色，避免误撤销自己的管理员权限。
        if (existing.getId().equals(currentUserId) && rolesChanged) {
            throw new BusinessException("不能修改自己的角色，避免误撤销自己的管理员权限，请让其他管理员调整");
        }
        // 兜底保护：本次修改若会让一名启用中的管理员失效，必须确认系统还有另一名启用管理员。
        if (isEnabledAdmin(existing) && (!newEnabled || !hasAdminRole(roleIds))
                && userMapper.countEnabledAdminsExcluding(existing.getId()) == 0) {
            throw new BusinessException("系统至少需要保留一名启用的管理员，请先为其他用户授予管理员角色并启用");
        }

        // 更新用户名。
        existing.setUsername(username);
        // 更新邮箱。
        existing.setEmail(email);
        // 更新可选电话，空白转为 null。
        existing.setPhone(trimToNull(form.getPhone()));
        // 采用上面规范化后的启用状态，未提交启用值时按停用处理。
        existing.setEnabled(newEnabled);
        if (StringUtils.hasText(form.getPassword())) {
            // 填写新密码时重新生成 BCrypt 摘要。
            existing.setPassword(passwordEncoder.encode(form.getPassword()));
        } else {
            // 留空时置为 null，使动态更新 SQL 跳过密码字段并保留原密码。
            existing.setPassword(null);
        }
        try {
            // 执行用户资料动态更新。
            userMapper.update(existing);
            // 删除旧角色关联，随后按最新选择重建，避免保留已取消的权限。
            userRoleMapper.deleteByUserId(existing.getId());
            // 逐项插入去重且验证过的角色关联。
            for (Long roleId : roleIds) {
                userRoleMapper.insert(existing.getId(), roleId);
            }
        } catch (DuplicateKeyException ex) {
            // 并发注册或更新可能绕过预检查，由数据库唯一约束拦截后转换为业务提示。
            throw new BusinessException("用户名或邮箱已存在", ex);
        }
    }

    /**
     * 删除没有历史领养申请的用户。
     *
     * @param id 待删除用户编号
     */
    @Transactional
    public void delete(Long id) {
        // 确认用户存在并取得其主键。
        User user = getById(id);
        // 有申请记录时禁止删除，保留申请人与历史审核数据的完整性。
        if (applicationMapper.countByUserId(id) > 0) {
            throw new BusinessException("该用户已提交领养申请，不能删除");
        }
        // 兜底保护：删除最后一名启用中的管理员会使后台无人可管理，直接拒绝。
        if (isEnabledAdmin(user) && userMapper.countEnabledAdminsExcluding(id) == 0) {
            throw new BusinessException("系统至少需要保留一名启用的管理员，不能删除最后一名启用的管理员");
        }
        // 先删除用户角色关联，避免遗留无用户的多对多记录。
        userRoleMapper.deleteByUserId(id);
        // 再删除用户主记录，两步在同一事务中完成。
        userMapper.deleteById(user.getId());
    }

    /**
     * 判断用户当前是否处于启用状态并拥有管理员角色。
     *
     * @param user 已加载角色集合的用户
     * @return 启用中的管理员返回 true
     */
    private boolean isEnabledAdmin(User user) {
        return Boolean.TRUE.equals(user.getEnabled())
                && user.getRoles() != null
                && user.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleName()));
    }

    /**
     * 判断待保存的角色集合中是否仍包含管理员角色。
     *
     * @param roleIds 去重并校验后的角色编号
     * @return 包含 ROLE_ADMIN 时返回 true
     */
    private boolean hasAdminRole(List<Long> roleIds) {
        Role adminRole = roleMapper.findByRoleName("ROLE_ADMIN");
        return adminRole != null && roleIds.contains(adminRole.getId());
    }

    /**
     * 在调用方事务内保存新用户及其角色。
     *
     * @param user 已规范化和加密的用户实体
     * @param roleIds 已确认有效的角色编号列表
     */
    private void saveNewUser(User user, List<Long> roleIds) {
        try {
            // 插入用户并回填数据库生成的主键。
            userMapper.insert(user);
            // 使用新主键逐项建立角色关联。
            for (Long roleId : roleIds) {
                userRoleMapper.insert(user.getId(), roleId);
            }
        } catch (DuplicateKeyException ex) {
            // 唯一索引处理预检查后仍可能发生的并发冲突，异常会触发外层事务回滚。
            throw new BusinessException("用户名或邮箱已存在", ex);
        }
    }

    /**
     * 对用户名和邮箱执行保存前唯一性预检查。
     * 此检查提升错误提示的准确性，但不能替代数据库唯一约束的并发保护。
     *
     * @param username 待保存用户名
     * @param email 已规范化邮箱
     * @param excludedId 更新时需要排除的当前用户编号；新增时传 null
     */
    private void ensureUnique(String username, String email, Long excludedId) {
        // 查询是否已有相同用户名。
        User byUsername = userMapper.findByUsername(username);
        // 找到其他编号即构成冲突；更新自己时允许保留原用户名。
        if (byUsername != null && !byUsername.getId().equals(excludedId)) {
            throw new BusinessException("用户名已存在");
        }
        // 查询是否已有相同邮箱。
        User byEmail = userMapper.findByEmail(email);
        // 找到其他编号即拒绝保存，避免多个账号共用邮箱。
        if (byEmail != null && !byEmail.getId().equals(excludedId)) {
            throw new BusinessException("邮箱已被使用");
        }
    }

    /**
     * 验证角色列表并去重。
     *
     * @param roleIds 表单提交的角色编号
     * @return 保持首次出现顺序的去重角色编号列表
     */
    private List<Long> validateRoles(List<Long> roleIds) {
        // 用户至少需要一个角色，防止创建无法获得任何权限的异常账号。
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException("请至少分配一个角色");
        }
        // LinkedHashSet 在去除重复编号的同时保持管理员选择顺序。
        List<Long> distinctIds = new ArrayList<>(new LinkedHashSet<>(roleIds));
        // 逐项检查空编号及不存在的角色，阻止伪造角色外键。
        for (Long roleId : distinctIds) {
            if (roleId == null || roleMapper.findById(roleId) == null) {
                throw new BusinessException("所选角色不存在");
            }
        }
        return distinctIds;
    }

    /**
     * 校验密码是否满足必填和最小长度要求。
     *
     * @param password 原始密码
     * @param required 新建时为 true，编辑时为 false
     */
    private void validatePassword(String password, boolean required) {
        // 新建场景拒绝 null、空串和纯空白密码。
        if (required && !StringUtils.hasText(password)) {
            throw new BusinessException("新用户密码不能为空");
        }
        // 只要实际填写了密码，无论新建或编辑都至少要求 6 个字符。
        if (StringUtils.hasText(password) && password.length() < 6) {
            throw new BusinessException("密码长度不能少于6个字符");
        }
    }

    /**
     * 规范化邮箱，便于唯一性检查和存储。
     *
     * @param email 原始邮箱
     * @return 去除首尾空白并按 Locale.ROOT 转为小写的邮箱
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 规范化可选文本字段。
     *
     * @param value 原始文本
     * @return 去除首尾空白后的文本；null 或纯空白返回 null
     */
    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
