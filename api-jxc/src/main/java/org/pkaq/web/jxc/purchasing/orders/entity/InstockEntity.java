package org.pkaq.web.jxc.instock.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.PureBaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 采购入库单
 * @author PKAQ
 */
@Data
@Alias("instock")
@TableName("t_jxc_instock")
@EqualsAndHashCode(callSuper = true)
@ApiModel("采购入库单")
public class InstockEntity extends PureBaseEntity {
    @ApiModelProperty("入库日期")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date indate;

    @ApiModelProperty("入库单号")
    private String incode;

    @ApiModelProperty("入库名称")
    private String title;

    /** 子表数据 **/
    @TableField(exist = false)
    private List<InstockLineEntity> line;
}