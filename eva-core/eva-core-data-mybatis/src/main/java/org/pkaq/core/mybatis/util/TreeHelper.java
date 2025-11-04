package org.pkaq.core.mybatis.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.pkaq.core.mvc.vo.StdTreeVo;
import org.pkaq.core.util.StrUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 树形数据工具类
 *
 * @author: S.PKAQ
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TreeHelper {
    /**
     * 组装路径
     * :注意: 这里如果子路径本身就是以与父路径相同开头的路径那么不会拼接父路径
     */
    public static String assemblePath(String parentPath, String currentPath, String oldParentPath) {
        if (StrUtils.isNotBlank(oldParentPath)) {
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
     * 将扁平化的节点列表转换为树形结构。
     * 适用于具有 ID、PID（父ID）、children 属性的数据模型，例如菜单、组织架构等。
     *
     * @param flatList 扁平结构的节点列表（无父子层级关系）
     * @param <T>      实现了树结构的实体类，需包含 getId、getPid、getChildren、setChildren 方法
     * @return 构建好的树形结构根节点列表
     */
    public static <T extends StdTreeVo> Collection<T> buildTree(Collection<T> flatList) {
        if (flatList == null || flatList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, T> idMap = flatList.stream()
                .collect(Collectors.toMap(T::getId, Function.identity(), (a, b) -> a));

        List<T> roots = new ArrayList<>();

        for (T node : flatList) {
            Long pid = node.getPid();
            if (pid == null || pid == 0 || !idMap.containsKey(pid)) {
                roots.add(node);
            } else {
                T parent = idMap.get(pid);
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(node);
            }
        }

        return roots;
    }

}
