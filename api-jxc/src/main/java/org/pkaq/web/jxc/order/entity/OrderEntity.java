package org.pkaq.web.jxc.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.Entity;

import java.util.Date;

/**
 *
 */
@Data
@Alias("order")
@TableName("t_order")
public class OrderEntity implements Entity {

    @TableId(type = IdType.INPUT)
    private Integer id;

    private Date month;

    private String note;
}