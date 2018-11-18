package org.pkaq.core.mvc.entity.tree;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/11/18 13:31
 */
public interface IBaseTreeEntity {

    String setId();

    String getId();

    String getKey();

    boolean getExact();

    String getParentId();

    String getParentName();

    String getPath();

    String getPathId();

    String getPathName();

    boolean isIsleaf();

    java.util.List<IBaseTreeEntity> getChildren();

    void setParentId(String parentId);

    void setParentName(String parentName);

    void setPath(String path);

    void setPathId(String pathId);

    void setPathName(String pathName);

    void setIsleaf(boolean isleaf);

    void setChildren(java.util.List<IBaseTreeEntity> children);

    void setKey(String key);

    void setExact(boolean exact);
}
