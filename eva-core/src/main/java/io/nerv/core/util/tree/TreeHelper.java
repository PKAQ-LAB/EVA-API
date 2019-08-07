package io.nerv.core.util.tree;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.mvc.entity.mybatis.BaseTreeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 树形数据工具类
 * @author: S.PKAQ
 * @Datetime: 2018/5/10 21:58
 */
public class TreeHelper {
    /**
     * 将list转换为树结构
     * @param moduleEntitys
     * @return
     */
    public List<BaseTreeEntity> bulid(List<BaseTreeEntity> moduleEntitys) {

        List<BaseTreeEntity> trees = new ArrayList<>();

        for (BaseTreeEntity entity : moduleEntitys) {
            String pid = entity.getParentId();
            if (StrUtil.isBlank(pid) || "0".equals(pid)) {
                trees.add(entity);
            }

            for (BaseTreeEntity it : moduleEntitys) {
                if (entity.getId().equals(it.getParentId())) {
                    if (entity.getOriginChildren() == null) {
                        entity.setChildren(new ArrayList<>());
                    }
                    entity.getOriginChildren().add(it);
                }
            }
        }
        return trees;
    }


    /**
     * 组装路径
     * :注意: 这里如果子路径本身就是以与父路径相同开头的路径那么不会拼接父路径
     * @param parentPath
     * @param currentPath
     * @return
     */
    public static String assemblePath(String parentPath, String currentPath,String oldParentPath) {
        if(StrUtil.isNotBlank(oldParentPath)) {
            //把子路径里原来的父路径替换成现在的父路径
            currentPath=currentPath.replace(oldParentPath,parentPath);
        }else {
            parentPath = (null != parentPath && parentPath.startsWith("/")) ? parentPath : "/" + parentPath;
            if (null != currentPath && !currentPath.startsWith(parentPath)) {
                currentPath = currentPath.startsWith("/") ? currentPath : "/" + currentPath;
                return parentPath + currentPath;
            }
        }
        return currentPath;
    }
}
