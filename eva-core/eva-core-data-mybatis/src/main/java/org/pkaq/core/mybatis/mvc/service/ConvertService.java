package org.pkaq.core.mybatis.mvc.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.pkaq.core.mvc.convert.Convert;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * service 基类
 * 定义一些公用的查询
 *
 * @author S.PKAQ
 */
public abstract class ConvertService<M extends BaseMapper, C extends Convert> {
    @Autowired
    protected M mapper;

    @Autowired
    protected C converter;
}
