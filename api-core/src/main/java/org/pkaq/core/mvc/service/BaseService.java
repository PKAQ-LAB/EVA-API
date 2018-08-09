package org.pkaq.core.mvc.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import org.pkaq.core.mvc.entity.BaseEntity;
import org.pkaq.core.mvc.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * service 基类
 * 抛出exception异常时 回滚事务
 * 定义一些公用的查询
 * Datetime: 2018/3/13 22:16
 * @author S.PKAQ
 */
@Transactional(rollbackFor = Exception.class)
public abstract class BaseService<M extends BaseMapper<T>, T extends BaseEntity> {
    @Autowired
    protected M mapper;
    /**
     * 根据ID查询
     * @param id id
     * @return 实体类对象
     */
    protected T getById(String id){
        return (T)this.mapper.selectById(id);
    }

    /**
     * 合并保存,如果不存在id执行插入,存在ID执行更新
     * @param entity 实体类对象
     */
    protected void merge(T entity){
        if (entity.getId() != null){
            this.mapper.insert(entity);
        } else {
            this.mapper.updateById(entity);
        }
    }

    /**
     * 查询所有
     * @param entity
     * @return
     */
    protected List<T> list(T entity){
        Wrapper<T> wrapper = new EntityWrapper<>(entity);
        return this.mapper.selectList(wrapper);
    }
    /**
     * 按分页查询
     * @param entity    目标实体类
     * @param page      当前页码
     * @return          分页模型类
     */
    protected Page<T> listPage(T entity, Integer page) {
        page = null != page ? page : 1;
        // 查询条件
        Wrapper<T> wrapper = new EntityWrapper<>(entity);
        // 分页条件
        Pagination pagination = new Pagination();
        pagination.setCurrent(page);
        // 分页查询
        Page<T> pager = new Page<>();
        pager.setData(this.mapper.selectPage(pagination,wrapper));
        BeanUtil.copyProperties(pagination, pager);
        return pager;
    }
    /**
     * 按分页查询
     * @param entity    目标实体类
     * @param page      当前页码
     * @param size      分页条数
     * @return          分页模型类
     */
    protected Page<T> listPage(T entity, Integer page, Integer size) {
        Wrapper<T> wrapper = new EntityWrapper<>(entity);
        Pagination pagination = new Pagination();
        pagination.setCurrent(page);
        pagination.setSize(size);

        Page<T> pager = new Page<>();
        pager.setData(this.mapper.selectPage(pagination,wrapper));
        BeanUtil.copyProperties(pagination, pager);

        return pager;
    }

}
