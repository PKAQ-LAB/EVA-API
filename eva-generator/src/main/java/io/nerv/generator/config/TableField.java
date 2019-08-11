package io.nerv.generator.config;

import cn.hutool.core.util.StrUtil;
import io.nerv.generator.dbtype.MySqlTypeConvert;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TableField {
   /** 字段名 **/
    private String columnName;

   /** 字段类型 **/
    private String columnType;

   /** 字段描述 **/
    private String comment;

   /** 字段长度 **/
    private String length;

   /** 是否允许为空 **/
    private boolean nullAble;

   /** 字典映射 **/
    private String codeMapping;

   /** 是否列表显示 **/
    private boolean listShow;

   /** 是否表单显示 **/
    private boolean formShow;

   /** 是否为查询条件 **/
    private boolean searchAble;

   /** 控件类型 **/
    private String compomentType;

   /** 显示名称 **/
    private String showName;

    public String getPropertyType(){
        return MySqlTypeConvert.getInstance().processTypeConvert(this.columnType).getType();
    }

    public String getPropertyName(){
        return StrUtil.toCamelCase(this.columnName);
    }
}
