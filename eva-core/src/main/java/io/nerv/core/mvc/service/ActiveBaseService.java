package io.nerv.core.mvc.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * service 基类
 * 抛出exception异常时 回滚事务
 * 定义一些公用的查询
 * Datetime: 2018/3/13 22:16
 * @author S.PKAQ
 */
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
public abstract class ActiveBaseService<M extends BaseMapper<T>, T extends Model> {
    @Autowired
    protected M mapper;
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
     */
    public void selectCount(T entity){
        Wrapper<T> wrapper = new QueryWrapper<>(entity);
        entity.selectCount(wrapper);
    }

    /**
     * 根据条件获取一条记录
     * @param entity
     */
    public void getByEntity(T entity){
        Wrapper<T> wrapper = new QueryWrapper<>(entity);
        entity.selectOne(wrapper);
    }
    /**
     * 合并保存,如果不存在id执行插入,存在ID执行更新
     * @param entity 实体类对象
     */
    public void merge(T entity){
        entity.insertOrUpdate();
    }
    /**
     * 查询所有
     * @param entity 要进行查询的实体类
     * @return 返回结果
     */
    protected List<T> list(T entity){
        QueryWrapper<T> wrapper = new QueryWrapper<>(entity);
        wrapper.orderByDesc("gmt_Modify");

        return entity.selectList(wrapper);
    }
    /**
     * 按分页查询
     * @param entity    目标实体类
     * @param page      当前页码
     * @param size      分页条数
     * @return          分页模型类
     */
    protected IPage<T> listPage(T entity, Integer page, Integer size) {

        QueryWrapper<T> wrapper = new QueryWrapper<>(entity);
        wrapper.orderByDesc("gmt_Modify");

        Page pagination = new Page();
        pagination.setCurrent(page);
        pagination.setSize(size);

        return entity.selectPage(pagination, wrapper);
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

        return entity.selectPage(pagination, wrapper);
    }
    /**
     * 通用删除
     * @param param
     */
    public void delete(ArrayList<String> param){
        this.mapper.deleteBatchIds(param);
    }
}
