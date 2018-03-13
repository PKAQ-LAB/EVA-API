package org.pkaq.core.mvc;

import com.baomidou.mybatisplus.annotations.TableId;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

/**
 * 实体类基类，用于存放公共属性
 * @author: S.PKAQ
 * @Datetime: 2018/3/13 22:48
 */
@Data
public abstract class BaseEntity implements Serializable {
    @TableId
    private String id;

    private String remark;

    private Date gmtCreate;

    private Date gmtModify;

    private String createBy;

    private String modifyBy;
}
