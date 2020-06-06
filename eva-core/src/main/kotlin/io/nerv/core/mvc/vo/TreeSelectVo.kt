package io.nerv.core.mvc.vo

/**
 * antd treeSelect需要的数据结构
 * @author: S.PKAQ
 * @Datetime: 2019/3/19 21:45
 */
open class TreeSelectVo {
    /** 全树唯一  */
    val key: String? = null

    /** 显示名称  */
    val title: String? = null

    /** 节点值  */
    val value: String? = null

    /** 是否叶子  */
    val isLeaf = false

    /** 路径  */
    val path: String? = null

    /** 路径中文描述  */
    val pathName: String? = null

    /** 上级节点id  */
    val parentId: String? = null

    /** 上级节点名称  */
    val parentName: String? = null

    /** 子节点  */
    val children: List<TreeSelectVo>? = null
}