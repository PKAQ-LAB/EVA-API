package org.pkaq.web.instock.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.BaseActiveEntity;

import java.util.Date;
import java.util.List;

/**
 * 采购入库单
 * @author PKAQ
 */
@Data
@Alias("instock")
@TableName("t_instock")
@EqualsAndHashCode(callSuper = true)
public class InstockEntity extends BaseActiveEntity {

    private Date indate;

    private String incode;

    private String remark;

    /** 子表数据 **/
    @TableField(exist = false)
    private List<InstockLineEntity> line;
}