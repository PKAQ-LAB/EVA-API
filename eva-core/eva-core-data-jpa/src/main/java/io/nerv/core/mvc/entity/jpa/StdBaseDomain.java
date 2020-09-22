package io.nerv.core.mvc.entity.jpa;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA 实体类基类
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class StdBaseDomain implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "snowflake")
    @GenericGenerator(name = "snowflake", strategy = "io.nerv.core.util.jpa.SnowflakeGenerator")
    @Id
    private String id;

    @ApiModelProperty("创建人")
    @CreatedBy
    private String createBy;

    @ApiModelProperty("创建时间")
    @CreatedDate
    private LocalDateTime gmtCreate;

    @ApiModelProperty("修改人")
    @LastModifiedBy
    private String modifyBy;

    @ApiModelProperty("修改时间")
    @LastModifiedDate
    private LocalDateTime gmtModify;

    @ApiModelProperty("备注")
    private String remark;
}
