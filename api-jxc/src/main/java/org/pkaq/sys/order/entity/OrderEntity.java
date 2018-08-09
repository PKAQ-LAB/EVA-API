package org.pkaq.sys.order.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.BaseEntity;
import org.pkaq.core.mvc.entity.Entity;

import java.util.Date;

/**
 *
 */
@Data
@Alias("order")
@TableName("t_order")
public class OrderEntity implements Entity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Date month;

    private String note;
}