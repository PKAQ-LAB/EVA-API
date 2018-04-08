package org.pkaq.core.mvc.entity;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Date;

/**
 * activeRecord 子表 基类
 * @author: S.PKAQ
 * @Datetime: 2018/3/28 7:16
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseLineActiveEntity<T extends Model> extends Model<T> implements Serializable{
    @TableId
    private String id;

    private String mainId;

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
