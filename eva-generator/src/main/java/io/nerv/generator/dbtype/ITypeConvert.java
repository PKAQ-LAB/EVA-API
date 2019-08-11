package io.nerv.generator.dbtype;

/**
 * 数据库字段类型转换
 */
public interface ITypeConvert {
    /**
     * 执行类型转换
     * @param fieldType    字段类型
     * @return ignore
     */
    DbColumnType processTypeConvert(String fieldType);
}
