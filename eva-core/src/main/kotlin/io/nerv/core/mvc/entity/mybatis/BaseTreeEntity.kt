package io.nerv.core.mvc.entity.mybatis

import cn.hutool.core.util.StrUtil
import com.baomidou.mybatisplus.annotation.TableField
import io.swagger.annotations.ApiModelProperty

/**
 * 树形结构实体基类
 * @author: S.PKAQ
 * @Datetime: 2018/11/18 13:20
 */
class BaseTreeEntity : StdBaseEntity() {
    @ApiModelProperty("上级节点id")
    var parentId: String? = null

    @ApiModelProperty("上级节点名称")
    var parentName: String? = null

    @ApiModelProperty("路径")
    var path: String? = null

    @ApiModelProperty("上级节点id路径")
    var pathId: String? = null

    @ApiModelProperty("上级节点名称路径")
    var pathName: String? = null

    @ApiModelProperty("是否叶子")
    var isleaf: Boolean? = null

    @TableField(exist = false)
    @ApiModelProperty("子节点")
    var children: MutableList<BaseTreeEntity>? = null
        get() =  if (children == null || this.children!!.size < 1) null else children

    @ApiModelProperty("key")
    @TableField(exist = false)
    var key: String? = null
        get() = this.id

    @ApiModelProperty("exact")
    @TableField(exist = false)
    var exact: Boolean? = null
        get() = this.isleaf;

    @TableField(exist = false)
    @ApiModelProperty("国际化面包屑")
    var locale: String? = null
        get() = if (StrUtil.isNotBlank(path)) "menu" + path!!.replace("/".toRegex(), ".") else ""

    fun getOriginChildren(): MutableList<BaseTreeEntity>? {
        return this.children
    }

}