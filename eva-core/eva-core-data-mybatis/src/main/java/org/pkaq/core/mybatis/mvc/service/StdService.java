package org.pkaq.core.mybatis.mvc.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogEnum;
import org.pkaq.core.mvc.bo.PageBo;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;
import org.pkaq.core.mybatis.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * service 基类
 * 抛出exception异常时 回滚事务
 * 定义一些公用的查询
 *
 * @author S.PKAQ
 */
public abstract class StdService<M extends BaseMapper<T>, T extends StdEntity> {
    @Autowired
    public M mapper;


    /**
     * 通用根据ID查询
     *
     * @param id id
     * @return 实体类对象
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "根据id查询")
    public T getById(String id) {
        return this.mapper.selectById(id);
    }

    /**
     * 查询符合条件得记录条数
     *
     * @param entity
     * @return
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "查询符合条件的记录条数")
    public Long selectCount(T entity) {
        Wrapper<T> wrapper = Wrappers.lambdaQuery(entity);
        return this.mapper.selectCount(wrapper);
    }

    /**
     * 根据条件获取一条记录
     *
     * @param entity
     * @return
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "根据条件获取一条记录")
    public T getByEntity(T entity) {
        Wrapper<T> wrapper = Wrappers.lambdaQuery(entity);
        return this.mapper.selectOne(wrapper);
    }

    /**
     * 合并保存,如果不存在id执行插入,存在ID执行更新
     *
     * @param entity 实体类对象
     */
    @BizLog(operateType = BizLogEnum.EDIT, description = "保存记录[{0}]", args = {"param:0.id"})
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void merge(T entity) {
        if (entity.getId() == null) {
            this.mapper.insert(entity);
        } else {
            this.mapper.updateById(entity);
        }
    }

    /***
     * 根据指定条件合并
     * @param entity
     * @param wrapper
     */
    @BizLog(operateType = BizLogEnum.EDIT, description = "保存记录[{0}]", args = {"param:0.id"})
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void merge(T entity, Wrapper<T> wrapper) {
        if (entity.getId() == null) {
            this.mapper.insert(entity);
        } else {
            this.mapper.update(entity, wrapper);
        }
    }

    /**
     * 查询所有
     *
     * @param entity 要进行查询的实体类
     * @return 返回结果
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "根据条件查询记录")
    public List<T> list(T entity) {
        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();
        wrapper.orderByDesc(T::getModifyBy);

        return this.mapper.selectList(wrapper);
    }

    /**
     * 按分页查询
     *
     * @param entity 目标实体类
     * @return 分页模型类
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "分页查询记录")
    public IPage<T> listPage(PageBo pageBo, T entity) {
        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();
        wrapper.setEntity(entity);
        wrapper.orderByDesc(T::getUtcModify);

        Page pagination = new Page(pageBo.getPageNo(), pageBo.getPageSize());
        return this.mapper.selectPage(pagination, wrapper);
    }

    /**
     * 通用分页查询
     *
     * @param entity 目标实体类
     * @param page   当前页码
     * @return 分页模型类
     */
    @BizLog(operateType = BizLogEnum.QUERY, description = "根据条件分页查询记录")
    public IPage<T> listPage(T entity, Integer page) {
        page = null != page ? page : 1;

        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();
        wrapper.setEntity(entity);
        wrapper.orderByDesc(T::getUtcModify);
        // 分页条件
        Page pagination = new Page();
        pagination.setCurrent(page);
        return this.mapper.selectPage(pagination, wrapper);
    }

    /**
     * 通用删除
     *
     * @param param
     * @return
     */
    @BizLog(operateType = BizLogEnum.DELETE, description = "删除记录[{0}]", args = {"param:0"})
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void delete(ArrayList<String> param) {
        this.mapper.deleteBatchIds(param);
    }
}
