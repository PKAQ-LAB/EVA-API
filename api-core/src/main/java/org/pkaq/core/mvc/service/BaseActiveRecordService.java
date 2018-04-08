package org.pkaq.core.mvc.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import org.pkaq.core.mvc.entity.BaseActiveEntity;
import org.pkaq.core.mvc.entity.BaseLineActiveEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/3/28 7:32
 */
@Transactional(rollbackFor = Exception.class)
public abstract class BaseActiveRecordService<M extends BaseMapper<T>, T extends BaseActiveEntity> {
    @Autowired
    protected M mapper;

    /**
     * 带子表的合并保存 ,如果不存在id执行插入,存在ID执行更新
     * @param entity 主表对象
     * @param line
     */
 protected void merge(T entity, List<? extends BaseLineActiveEntity> line){
        boolean isNew = false;
        if (StrUtil.isBlank(entity.getId())) {
            isNew = true;
        }
        entity.insertOrUpdate();
        // 判断是否存在子表
        if (CollectionUtil.isNotEmpty(line)){
            for (BaseLineActiveEntity t : line) {
                t.setMainId(entity.getId());
                if (isNew) {
                    t.insert();
                } else {
                   t.updateById();
                }
            }
        }
    }
}
