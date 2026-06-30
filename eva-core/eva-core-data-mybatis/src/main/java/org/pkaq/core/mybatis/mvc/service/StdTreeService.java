package org.pkaq.core.mybatis.mvc.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.DelEnumm;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.mybatis.mvc.entity.StdTreeEntity;
import org.pkaq.core.mybatis.mvc.mapper.StdTreeMapper;
import org.pkaq.core.util.CollUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * service 基类
 * 抛出exception异常时 回滚事务
 * 定义一些公用的查询
 *
 * @author S.PKAQ
 */
public abstract class StdTreeService<M extends StdTreeMapper<T>, T extends StdTreeEntity> {
    @Autowired
    public M mapper;

    /**
     * 查询结构树
     *
     * @return
     */
    public List<T> lisTree(T entity) {
        return this.mapper.listTree(entity);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void merge(T entity) {
        if (entity.getId() == null) {
            this.mapper.insert(entity);
        } else {
            this.mapper.updateById(entity);
        }
    }

    /**
     * 根据ID批量删除
     *
     * @param ids
     * @return
     */
    public Response<?> delete(Set<String> ids) {
        Response<?> response = null;
        // 检查是否存在子节点，存在子节点不允许删除
        LambdaQueryWrapper<T> oew = Wrappers.lambdaQuery();
        oew.in(T::getPid, ids);

        List<T> leafList = this.mapper.selectList(oew);

        if (CollUtils.isNotEmpty(leafList)) {
            List<Object> list = CollUtils.getFieldValues(leafList, "parentName");
            String name = CollUtils.join(list, ",");
            response = new Response().failure(CommonCodes.CHILD_EXIST, name);
        } else {
            this.mapper.deleteBatchIds(ids);
            response = new Response().success();
        }

        return response;
    }

    /**
     * 新增/编辑一条信息
     *
     * @param entity 要 新增/编辑 的对象
     */
    public void edit(T entity) {
        Long orgId = entity.getId();
        // 获取上级节点
        Long pid = entity.getPid();
        if (null != pid && pid != 0) {
            // 查询新父节点信息
            T parent = this.get(pid);
            // 设置当前节点信息
            String parentPath = entity.getPath() + "/" + entity.getId();
            entity.setPath(parentPath);

        } else {
            // 父节点为空, 根节点 设置为非叶子\
            pid = null;
            if (null != entity.getId() && entity.getId() != 0) {
                entity.setPath(entity.getId() + "");
            }
            entity.setPid(pid);
            entity.setIsleaf(false);
        }

        // 检查原父节点是否还存在子节点 不存在设置leaf为false
        T orginNode = this.mapper.getParentById(orgId);

        // 如果更换了父节点 重新确定原父节点的 leaf属性，以及所修改节点的orders属性
        if (null != orginNode && !Objects.equals(pid, orginNode.getPid())) {
            int brothers = this.mapper.countPrantLeaf(orgId) - 1;
            if (brothers < 1) {
                orginNode.setIsleaf(true);
                this.update(orginNode);
            }
        }
        //如果是新增且orders属性为空则设置orders属性
        T oldOrgin = null;
        if (null == entity.getId() || entity.getId() == 0) {
            LambdaQueryWrapper<T> orderQuery = new LambdaQueryWrapper<>();
            orderQuery.eq(T::getPid, pid);
            orderQuery.eq(T::getDeleted, DelEnumm.UN_DELETED);

            entity.setSort((double) this.mapper.selectCount(orderQuery));
        } else {
            oldOrgin = this.mapper.selectById(orgId);
        }
        this.merge(entity);

        //新增
        if (null == oldOrgin) {
            //设置path路径 把path路径加上自己本身
            //String path= StrUtils.isBlank(organization.getPath()) ? organization.getId() : organization.getPath() + "/" + organization.getId();
            this.mapper.updateById(entity);
        } else {
            //刷新子节点相关数据
            this.refreshChild(entity, oldOrgin);
        }
        // 保存完重新查询一遍列表数据
    }

    // 父节点信息有修改 刷新子节点相关数据
    public void refreshChild(T entity, T origin) {
        // 刷新子节点名称
        this.mapper.updateChildParentName(entity.getName(), entity.getId());
        // TODO 刷新所有子节点的 path_name 和 path
        this.mapper.updateChildPathInfo(entity, origin);
    }

    /**
     * 根据ID更新
     *
     * @param entity
     */
    public void update(T entity) {
        // 检查是否存在叶子节点，存在 返回叶子节点名称 终止删除
        this.mapper.updateById(entity);
    }

    /**
     * 根据ID获取一条
     *
     * @param id ID
     * @return
     */
    public T get(Long id) {
        return this.mapper.selectById(id);
    }

    /**
     * 交换两个orders值
     *
     * @param entity 进行交换的两个实体
     */
    public void swtich(T[] entity) {
        for (T org : entity) {
            this.mapper.updateById(org);
        }
    }

    /**
     * 切换可用状态 - 级联操作
     *
     * @param entity
     */
    public void changeStatus(T entity) {
        this.mapper.changeStatus(entity);
    }

    /**
     * 查询符合条件的结果条数
     *
     * @param entity
     * @return
     */
    public Long count(T entity) {
        return this.mapper.selectCount(Wrappers.lambdaQuery());
    }
}
