package org.pkaq.core.mvc.vo;

import lombok.Data;

import java.util.List;

/**
 * antd treeSelect需要的数据结构
 *
 * @author PKAQ
 */
@Data
public class TreeSelectVo {
    /**
     * 全树唯一
     **/
    private String key;
    /**
     * 显示名称
     **/
    private String title;
    /**
     * 节点值
     **/
    private String value;
    /**
     * 是否叶子
     **/
    private boolean isLeaf;
    /**
     * 路径
     **/
    private String path;
    /**
     * 上级节点id
     **/
    private String pid;
    /**
     * 子节点
     **/
    private List<TreeSelectVo> children;
}
