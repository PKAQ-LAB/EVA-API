package io.nerv.core.mvc.service.mybatis;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.nerv.core.mvc.entity.mybatis.StdBaseEntity;
import io.nerv.core.mvc.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * service 基类
 * 抛出exception异常时 回滚事务
 * 定义一些公用的查询
 * @author S.PKAQ
 */
public abstract class StdBaseService<M extends BaseMapper<T>, T extends StdBaseEntity> {
    @Autowired
    public M mapper;
    /**
     * 通用根据ID查询
     * @param id id
     * @return 实体类对象
     */
    public T getById(String id){
        return this.mapper.selectById(id);
    }
    /**
     * 查询符合条件得记录条数
     * @param entity
     * @return
     */
    public Integer selectCount(T entity){
        Wrapper<T> wrapper = new QueryWrapper<>(entity);
        return this.mapper.selectCount(wrapper);
    }
    /**
     * 根据条件获取一条记录
     * @param entity
     * @return
     */
    public T getByEntity(T entity){
        Wrapper<T> wrapper = new QueryWrapper<>(entity);
        return this.mapper.selectOne(wrapper);
    }
    /**
     * 合并保存,如果不存在id执行插入,存在ID执行更新
     * @param entity 实体类对象
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void merge(T entity){
        if (entity.getId() == null){
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
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void merge(T entity, Wrapper<T> wrapper){
        if (entity.getId() == null){
            this.mapper.insert(entity);
        } else {
            this.mapper.update(entity, wrapper);
        }
    }
    /**
     * 查询所有
     * @param entity 要进行查询的实体类
     * @return 返回结果
     */
    public List<T> list(T entity){
        QueryWrapper<T> wrapper = new QueryWrapper<>(entity);
        wrapper.orderByDesc("gmt_Modify");

        return this.mapper.selectList(wrapper);
    }
    /**
     * 按分页查询
     * @param entity    目标实体类
     * @param page      当前页码
     * @param size      分页条数
     * @return          分页模型类
     */
    public IPage<T> listPage(T entity, Integer page, Integer size) {
        page = null != page ? page : 1;
        size = null != size ? size : 10;

        QueryWrapper<T> wrapper = new QueryWrapper<>(entity);
        wrapper.orderByDesc("gmt_Modify");

        Page pagination = new Page();
        pagination.setCurrent(page);
        pagination.setSize(size);

        return this.mapper.selectPage(pagination,wrapper);
    }

    /**
     * 通用分页查询
     * @param entity    目标实体类
     * @param page      当前页码
     * @return          分页模型类
     */
    public IPage<T> listPage(T entity, Integer page) {
        page = null != page ? page : 1;
        // 查询条件
        QueryWrapper<T> wrapper = new QueryWrapper<>(entity);
        wrapper.orderByDesc("gmt_Modify");
        // 分页条件
        Page pagination = new Page();
        pagination.setCurrent(page);
        return this.mapper.selectPage(pagination,wrapper);
    }
    /**
     * 通用删除
     * @param param
     * @return
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void delete(ArrayList<String> param){
        this.mapper.deleteBatchIds(param);
    }
}
