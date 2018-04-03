package org.pkaq.core.mvc.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.pkaq.core.mvc.entity.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
        if (StrUtil.isBlank(entity.getId())){
            this.mapper.insert(entity);
        } else {
            this.mapper.updateById(entity);
        }
    }
}
