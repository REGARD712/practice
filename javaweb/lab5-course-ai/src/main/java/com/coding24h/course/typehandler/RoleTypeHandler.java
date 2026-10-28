package com.coding24h.course.typehandler;

import com.coding24h.course.entity.User;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(User.Role.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class RoleTypeHandler extends BaseTypeHandler<User.Role> {
    @Override
    public void setNonNullParameter(PreparedStatement statement, int index, User.Role role, JdbcType jdbcType)
            throws SQLException {
        statement.setString(index, role.getValue());
    }

    @Override
    public User.Role getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return roleFrom(resultSet.getString(columnName));
    }

    @Override
    public User.Role getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return roleFrom(resultSet.getString(columnIndex));
    }

    @Override
    public User.Role getNullableResult(CallableStatement statement, int columnIndex) throws SQLException {
        return roleFrom(statement.getString(columnIndex));
    }

    private User.Role roleFrom(String value) {
        return value == null ? null : User.Role.fromValue(value);
    }
}
