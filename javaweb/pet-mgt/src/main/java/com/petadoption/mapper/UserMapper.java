package com.petadoption.mapper;

import com.petadoption.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户表 user 的 MyBatis Mapper，负责用户查询、新增、动态更新和删除。
 * SQL 中的 `user` 使用反引号包裹，以避免数据库关键字或保留字带来的兼容问题。
 */
public interface UserMapper {
    /**
     * 定义名为 userResultMap 的复用结果映射，将数据库下划线列名映射到 User 的驼峰属性。
     * roles 是一对多关联：以当前行的 id 为参数调用 RoleMapper.findByUserId，查询 user_role 与 role 表。
     */
    @Results(id = "userResultMap", value = {
            // id = true 将 id 标识为结果对象的主键映射。
            @Result(id = true, property = "id", column = "id"),
            // 登录用户名直接对应同名列。
            @Result(property = "username", column = "username"),
            // 密码字段直接对应 password 列。
            @Result(property = "password", column = "password"),
            // 邮箱字段直接对应 email 列。
            @Result(property = "email", column = "email"),
            // 联系电话直接对应 phone 列。
            @Result(property = "phone", column = "phone"),
            // 头像路径直接对应 avatar 列。
            @Result(property = "avatar", column = "avatar"),
            // 启用标志直接对应 enabled 列。
            @Result(property = "enabled", column = "enabled"),
            // 将下划线列 created_at 映射为 createdAt 属性。
            @Result(property = "createdAt", column = "created_at"),
            // 把当前用户 id 传给角色查询，返回的 Role 列表填充 roles 属性。
            @Result(property = "roles", column = "id",
                    many = @Many(select = "com.petadoption.mapper.RoleMapper.findByUserId"))
    })
    /** 按用户名精确查询用户，并通过 userResultMap 同时加载角色集合；未找到时返回 null。 */
    @Select("SELECT * FROM `user` WHERE username = #{username}")
    User findByUsername(String username);

    // 复用上面声明的结果映射，避免重复书写字段及角色关联配置。
    @ResultMap("userResultMap")
    /** 按用户主键查询单个用户并加载角色集合，未找到时返回 null。 */
    @Select("SELECT * FROM `user` WHERE id = #{id}")
    User findById(Long id);

    // 使用统一结果映射转换用户字段并加载每个用户的角色。
    @ResultMap("userResultMap")
    /** 查询全部用户，按创建时间倒序、主键倒序排列，保证新用户优先且顺序稳定。 */
    @Select("SELECT * FROM `user` ORDER BY created_at DESC, id DESC")
    List<User> findAll();

    /**
     * 按邮箱精确查询用户基础字段，未找到时返回 null。
     * 此方法未引用 userResultMap，因此不会通过该映射触发角色集合查询。
     */
    @Select("SELECT * FROM `user` WHERE email = #{email}")
    User findByEmail(String email);

    /** 统计 user 表中的用户总数。 */
    @Select("SELECT COUNT(*) FROM `user`")
    long count();

    /**
     * 插入用户基础资料，所有 VALUES 参数均从传入的 User 对象读取。
     * created_at 未出现在字段列表中，由数据库默认值负责生成。
     */
    @Insert("""
            INSERT INTO `user` (username, password, email, phone, avatar, enabled)
            VALUES (#{username}, #{password}, #{email}, #{phone}, #{avatar}, #{enabled})
            """)
    // 获取数据库自增生成的用户主键，并回填到 user.id。
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 按 id 更新用户资料，返回受影响行数。
     * username、email、phone、enabled 始终更新；password 使用动态 SQL：仅当其非 null 且不是空字符串时，
     * 才追加 password 赋值。这样编辑用户时将密码留空即可保留原密码，避免空值覆盖已有密码。
     */
    @Update("""
            <script>
            UPDATE `user`
            SET username = #{username},
                email = #{email},
                phone = #{phone},
                enabled = #{enabled}
            <if test="password != null and password != ''">
                , password = #{password}
            </if>
            WHERE id = #{id}
            </script>
            """)
    int update(User user);

    /** 按主键删除用户记录，返回实际删除的行数。 */
    @Delete("DELETE FROM `user` WHERE id = #{id}")
    int deleteById(Long id);
}
