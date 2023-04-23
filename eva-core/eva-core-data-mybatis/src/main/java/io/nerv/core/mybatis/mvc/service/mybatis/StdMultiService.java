package io.nerv.core.mybatis.mvc.service.mybatis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.nerv.core.mybatis.mvc.entity.mybatis.StdMultiEntity;
import io.nerv.core.mybatis.mvc.entity.mybatis.StdMultiLineEntity;
import io.nerv.core.mybatis.mvc.util.Page;
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
public abstract class StdMultiService<M extends BaseMapper<T>,
        L extends BaseMapper<S>,
        T extends StdMultiEntity<S>,
        S extends StdMultiLineEntity> {
    @Autowired
    public M mapper;

    @Autowired
    public L lineMapper;

    /**
     * 通用根据ID查询
     *
     * @param id id
     * @return 实体类对象
     */
    public T getById(String id) {
        T main = this.mapper.selectById(id);
        LambdaQueryWrapper<S> q = Wrappers.lambdaQuery();
        q.eq(S::getMainId, id);

        if (null == main) return null;

        List<S> line = this.lineMapper.selectList(q);
        main.setLines(line);

        return main;
    }

    /**
     * 根据条件获取一条记录
     *
     * @param entity
     * @return
     */
    public T getByEntity(T entity) {
        T main = this.mapper.selectOne(Wrappers.lambdaQuery(entity));
        LambdaQueryWrapper<S> q = Wrappers.lambdaQuery();
        q.eq(S::getMainId, main.getId());

        if (null == main) return null;

        List<S> line = this.lineMapper.selectList(q);
        main.setLines(line);

        return main;
    }

    /**
     * 获取主表明细
     *
     * @param id
     * @return
     */
    public T getMain(String id) {
        return this.mapper.selectById(id);
    }

    /**
     * 查询子表数据
     *
     * @param mainId
     * @return
     */
    public List<S> getLine(String mainId) {
        LambdaQueryWrapper<S> q = Wrappers.lambdaQuery();
        q.eq(S::getMainId, mainId);
        return this.lineMapper.selectList(q);
    }

    /**
     * 合并保存,如果不存在id执行插入,存在ID执行更新
     *
     * @param entity 实体类对象
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void merge(T entity) {
        String id = entity.getId();
        // 校验code唯一性

        if (StrUtil.isBlank(id)) {
            // 保存主表
            String mainId = IdWorker.getIdStr();

            entity.setId(mainId);
            this.mapper.insert(entity);
            // 保存子表
            if (CollUtil.isNotEmpty(entity.getLines())) {
                entity.getLines().forEach(item -> {
                    item.setMainId(mainId);
                    lineMapper.insert(item);
                });
            }
        } else {
            this.mapper.updateById(entity);
            // 更新子表， 先删除再插入
            if (CollUtil.isNotEmpty(entity.getLines())) {
                LambdaQueryWrapper<S> deleteWrapper = Wrappers.lambdaQuery();
                deleteWrapper.eq(S::getMainId, id);
                this.lineMapper.delete(deleteWrapper);

                entity.getLines().forEach(item -> {
                    item.setMainId(id);
                    lineMapper.insert(item);
                });
            }
        }
    }

    /**
     * 查询所有
     *
     * @param entity 要进行查询的实体类
     * @return 返回结果
     */
    public List<T> list(T entity) {
        LambdaQueryWrapper<T> q = Wrappers.lambdaQuery(entity);
        return this.mapper.selectList(q);
    }


    /**
     * 按分页查询
     *
     * @param entity 目标实体类
     * @param page   当前页码
     * @param size   分页条数
     * @return 分页模型类
     */
    public IPage<T> listPage(T entity, Integer page, Integer size) {
        page = null != page ? page : 1;
        size = null != size ? size : 10;

        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery(entity);
        wrapper.orderByDesc(T::getGmtModify);

        Page pagination = new Page();
        pagination.setCurrent(page);
        pagination.setSize(size);

        return this.mapper.selectPage(pagination, wrapper);
    }

    /**
     * 通用分页查询
     *
     * @param entity 目标实体类
     * @param page   当前页码
     * @return 分页模型类
     */
    public IPage<T> listPage(T entity, Integer page) {
        page = null != page ? page : 1;
        // 查询条件
        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery(entity);
        wrapper.orderByDesc(T::getGmtModify);
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
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void delete(ArrayList<String> param) {
        // 先删除子表 再删除主表
        LambdaQueryWrapper<S> lineWrapper = Wrappers.lambdaQuery();
        lineWrapper.in(S::getMainId, param);
        this.lineMapper.delete(lineWrapper);

        this.mapper.deleteBatchIds(param);
    }


}
