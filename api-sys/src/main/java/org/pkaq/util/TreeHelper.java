package org.pkaq.util;

import cn.hutool.core.util.StrUtil;
import org.pkaq.sys.module.entity.ModuleEntity;

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
     * @param moduleentitys
     * @return
     */
    public List<ModuleEntity> bulid(List<ModuleEntity> moduleentitys) {

        List<ModuleEntity> trees = new ArrayList<>();

        for (ModuleEntity entity : moduleentitys) {
            String pid = entity.getParentId();
            if (StrUtil.isBlank(pid) || "0".equals(pid)) {
                trees.add(entity);
            }

            for (ModuleEntity it : moduleentitys) {
                if (entity.getId().equals(it.getParentId())) {
                    if (entity.getChildren() == null) {
                        entity.setChildren(new ArrayList<>());
                    }
                    entity.getChildren().add(it);
                }
            }
        }
        return trees;
    }
}
