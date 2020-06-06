package io.nerv.core.util.tree

import cn.hutool.core.util.StrUtil
import io.nerv.core.mvc.entity.mybatis.BaseTreeEntity
import java.util.*

/**
 * 树形数据工具类
 * @author: S.PKAQ
 * @Datetime: 2018/5/10 21:58
 */
class TreeHelper {
    /**
     * 将list转换为树结构
     * @param moduleEntitys
     * @return
     */
    fun bulid(moduleEntitys: List<BaseTreeEntity>): List<BaseTreeEntity> {
        val trees: MutableList<BaseTreeEntity> = ArrayList()
        for (entity in moduleEntitys) {
            val pid: String = entity.getParentId()
            if (StrUtil.isBlank(pid) || "0" == pid) {
                trees.add(entity)
            }
            for (it in moduleEntitys) {
                if (entity.getId().equals(it.getParentId())) {
                    if (entity.originChildren == null) {
                        entity.setChildren(ArrayList<E>())
                    }
                    entity.originChildren.add(it)
                }
            }
        }
        return trees
    }

    companion object {
        /**
         * 组装路径
         * :注意: 这里如果子路径本身就是以与父路径相同开头的路径那么不会拼接父路径
         * @param parentPath
         * @param currentPath
         * @return
         */
        @kotlin.jvm.JvmStatic
        fun assemblePath(parentPath: String?, currentPath: String?, oldParentPath: String?): String? {
            var parentPath = parentPath
            var currentPath = currentPath
            if (StrUtil.isNotBlank(oldParentPath)) {
                //把子路径里原来的父路径替换成现在的父路径
                currentPath = currentPath!!.replace(oldParentPath!!, parentPath!!)
            } else {
                parentPath = if (null != parentPath && parentPath.startsWith("/")) parentPath else "/$parentPath"
                if (null != currentPath && !currentPath.startsWith(parentPath)) {
                    currentPath = if (currentPath.startsWith("/")) currentPath else "/$currentPath"
                    return parentPath + currentPath
                }
            }
            return currentPath
        }
    }
}