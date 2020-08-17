package io.nerv.core.mvc.entity.jpa

import io.swagger.annotations.ApiModelProperty
import lombok.Data
import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

/**
 * JPA 实体类基类
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class StdBaseDomain : Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "snowflake")
    @GenericGenerator(name = "snowflake", strategy = "io.nerv.core.util.jpa.SnowflakeGenerator")
    @Id
    private val id: String? = null

    @ApiModelProperty("创建人")
    @CreatedBy
    private val createBy: String? = null

    @ApiModelProperty("创建时间")
    @CreatedDate
    private val gmtCreate: LocalDateTime? = null

    @ApiModelProperty("修改人")
    @LastModifiedBy
    private val modifyBy: String? = null

    @ApiModelProperty("修改时间")
    @LastModifiedDate
    private val gmtModify: LocalDateTime? = null

    @ApiModelProperty("备注")
    private val remark: String? = null
}