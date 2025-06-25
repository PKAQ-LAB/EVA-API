package org.pkaq.core.mybatis.util;

import cn.hutool.core.text.CharSequenceUtil;
import org.pkaq.core.mybatis.mvc.entity.StdTreeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 树形数据工具类
 *
 * @author: S.PKAQ
 */
public class TreeHelper {
    /**
     * 组装路径
     * :注意: 这里如果子路径本身就是以与父路径相同开头的路径那么不会拼接父路径
     *
     */
    public static String assemblePath(String parentPath, String currentPath, String oldParentPath) {
        if (CharSequenceUtil.isNotBlank(oldParentPath)) {
            //把子路径里原来的父路径替换成现在的父路径
            currentPath = currentPath.replace(oldParentPath, parentPath);
        } else {
            parentPath = (null != parentPath && parentPath.startsWith("/")) ? parentPath : "/" + parentPath;
            if (null != currentPath && !currentPath.startsWith(parentPath)) {
                currentPath = currentPath.startsWith("/") ? currentPath : "/" + currentPath;
                return parentPath + currentPath;
            }
        }
        return currentPath;
    }

    /**
     * 将list转换为树结构
     *
     */
    public List<StdTreeEntity> bulid(List<? extends StdTreeEntity> moduleEntitys) {

        List<StdTreeEntity> trees = new ArrayList<>();

        for (StdTreeEntity entity : moduleEntitys) {
            Long pid = entity.getPid();
            if (null == pid || 0 == pid) {
                trees.add(entity);
            }

            for (StdTreeEntity it : moduleEntitys) {
                if (entity.getId().equals(it.getPid())) {
//                    if (entity.getOriginChildren() == null) {
//                        entity.setChildren(new ArrayList<>());
//                    }
//                    entity.getOriginChildren().add(it);
                }
            }
        }
        return trees;
    }
}
