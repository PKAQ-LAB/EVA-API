package org.pkaq.core.mvc.entity;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableId;
import com.sun.org.apache.xpath.internal.operations.Mod;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

/**
 * activeRecord 基类
 * @author: S.PKAQ
 * @Datetime: 2018/3/28 7:16
 */
@Data
public class BaseActiveEntity<T extends Model> extends Model<T> implements Serializable{
    @TableId
    private String id;

    private String remark;

    private Date gmtCreate;

    private Date gmtModify;

    private String createBy;

    private String modifyBy;
    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}
