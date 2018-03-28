package org.pkaq.core.mvc.service;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.pkaq.core.mvc.entity.BaseActiveEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: S.PKAQ
 * @Datetime: 2018/3/28 7:32
 */
@Transactional(rollbackFor = Exception.class)
public class BaseActiveRecordService<M extends BaseMapper<T>, T extends BaseActiveEntity> {
    @Autowired
    protected M mapper;
}
