package io.nerv.core.mvc.entity.mybatis

import cn.hutool.core.util.StrUtil
import com.baomidou.mybatisplus.annotation.TableField
import io.swagger.annotations.ApiModelProperty
import lombok.Data
import lombok.EqualsAndHashCode

/**
 * 树形结构实体基类
 * @author: S.PKAQ
 * @Datetime: 2018/11/18 13:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
open class BaseTreeEntity : StdBaseEntity() {
    @ApiModelProperty("上级节点id")
    private val parentId: String? = null

    @ApiModelProperty("上级节点名称")
    private val parentName: String? = null

    @ApiModelProperty("路径")
    private val path: String? = null

    @ApiModelProperty("上级节点id路径")
    private val pathId: String? = null

    @ApiModelProperty("上级节点名称路径")
    private val pathName: String? = null

    @ApiModelProperty("是否叶子")
    private val isleaf: Boolean? = null

    @TableField(exist = false)
    @ApiModelProperty("子节点")
    val originChildren: List<BaseTreeEntity>? = null

    @ApiModelProperty("key")
    @TableField(exist = false)
    open val key: String?
        get() = id

    @ApiModelProperty("exact")
    @TableField(exist = false)
    private val exact: Boolean? = null

    @TableField(exist = false)
    @ApiModelProperty("国际化面包屑")
    val locale: String?
        get() = if (StrUtil.isNotBlank(path)) "menu" + path!!.replace("/".toRegex(), ".") else ""

    fun getExact(): Boolean? {
        return isleaf
    }

    fun getChildren(): List<BaseTreeEntity>? {
        return if (originChildren == null || originChildren.size < 1) null else originChildren
    }

}