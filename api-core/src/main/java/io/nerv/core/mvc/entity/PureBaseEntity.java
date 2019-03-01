package io.nerv.core.mvc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 实体类基类，用于存放公共属性
 * @author: S.PKAQ
 * @Datetime: 2018/3/13 22:48
 */
@Data
public abstract class PureBaseEntity implements Entity, Serializable {
    @TableId(type = IdType.UUID)
    private String id;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("创建时间")
    private Date gmtCreate;

    @ApiModelProperty("修改人")
    private String modifyBy;

    @ApiModelProperty("修改时间")
    private Date gmtModify;

    @ApiModelProperty("备注")
    private String remark;
}
