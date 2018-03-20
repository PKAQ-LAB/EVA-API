package org.pkaq.core.mvc;

import com.baomidou.mybatisplus.mapper.BaseMapper;
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
     * @param id
     * @return
     */
    protected T getById(String id){
        return (T)this.mapper.selectById(id);
    }

}
