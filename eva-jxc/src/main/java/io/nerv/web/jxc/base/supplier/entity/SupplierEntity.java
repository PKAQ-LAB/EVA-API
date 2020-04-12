package io.nerv.web.jxc.base.supplier.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.nerv.core.mvc.entity.mybatis.StdBaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

/**
 * 供应商管理
 * @author PKAQ
 */
@Data
@Alias("supplier")
@ApiModel
@TableName("JXC_BASE_SUPPLIIERS")
@EqualsAndHashCode(callSuper = true)
public class SupplierEntity extends StdBaseEntity {
    @ApiModelProperty("全称")
    @TableField(condition = SqlCondition.LIKE)
    private String fullName;

    @ApiModelProperty("简称")
    private String name;

    @ApiModelProperty("助记码")
    private String mnemonic;

    @ApiModelProperty("类型(0001 生产厂家，0002 代理商)")
    private String category;

    @ApiModelProperty("联系人")
    private String linkman;

    @ApiModelProperty("联系方式")
    private String mobile;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("状态")
    @TableLogic
    private String status = "0000";
};