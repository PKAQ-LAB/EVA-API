package io.nerv.core.mybatis.mvc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.core.mybatis.mvc.entity.mybatis.StdTreeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字典管理mapper
 *
 * @author S.PKAQ
 */
@Mapper
@Repository
public interface StdTreeMapper<T> extends BaseMapper<T> {

    /**
     * 更改状态
     *
     * @param entity
     */
    void changeStatus(T entity);

    /**
     * 刷新子节点的上级节点名称
     *
     * @param name
     * @param id
     */
    void updateChildParentName(String name, String id);

    /**
     * 刷新子节点的路径
     *
     * @param entity
     * @param origin
     */
    void updateChildPathInfo(T entity, T origin);

    /**
     * 子节点个数
     *
     * @param pid
     * @return
     */
    int countPrantLeaf(String pid);

    /**
     * 查询子节点
     *
     * @param id
     * @param <T>
     * @return
     */
    <T extends StdTreeEntity> T listChildren(String id);

    /**
     * 根据id查询父节点
     *
     * @param orgId
     * @param <T>
     * @return
     */
    <T extends StdTreeEntity> T getParentById(String orgId);

    /**
     * 加载树结构
     *
     * @param entity
     * @param <T>
     * @return
     */
    <T extends StdTreeEntity> List<T> listTree(T entity);
}