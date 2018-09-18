package org.pkaq.core.mvc.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Date;

/**
 * activeRecord 基类
 * @author: S.PKAQ
 * @Datetime: 2018/3/28 7:16
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseActiveEntity<T extends Model> extends Model<T> implements Entity, Serializable{
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
