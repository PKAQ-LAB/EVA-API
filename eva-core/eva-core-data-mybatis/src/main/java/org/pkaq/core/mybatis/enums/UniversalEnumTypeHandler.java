package org.pkaq.core.mybatis.enums;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.pkaq.core.enums.BaseEnum;
import org.pkaq.core.enums.DelEnumm;
import org.pkaq.core.enums.FrozenEnumm;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * 通用枚举处理器
 * 自动将数据库中的 code ↔ 枚举对象 映射
 */
@MappedTypes({FrozenEnumm.class, DelEnumm.class})
public class UniversalEnumTypeHandler<E extends Enum<E> & BaseEnum<?>> extends BaseTypeHandler<E> {

    private Class<E> type;

    /**
     * ⚠️ 必须要有无参构造，MyBatis 会通过反射调用这个构造函数
     */
    public UniversalEnumTypeHandler() {
    }

    /**
     * MyBatis 也可能通过有参构造来传递枚举类型
     */
    public UniversalEnumTypeHandler(Class<E> type) {
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setObject(i, parameter.getCode());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object code = rs.getObject(columnName);
        return code == null ? null : codeOf(code);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object code = rs.getObject(columnIndex);
        return code == null ? null : codeOf(code);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object code = cs.getObject(columnIndex);
        return code == null ? null : codeOf(code);
    }

    private E codeOf(Object code) {
        if (type == null) {
            // MyBatis 3.5.9+ 会通过有参构造传递 type，不会走这里
            throw new IllegalStateException("Enum type not initialized in UniversalEnumTypeHandler");
        }

        for (E e : type.getEnumConstants()) {
            if (Objects.equals(((BaseEnum<?>) e).getCode(), code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("No enum constant " + type.getName() + "." + code);
    }
}
