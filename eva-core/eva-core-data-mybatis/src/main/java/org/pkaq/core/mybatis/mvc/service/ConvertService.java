package org.pkaq.core.mybatis.mvc.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jakarta.annotation.Resource;
import org.pkaq.core.mvc.convert.Convert;

/**
 * service 基类
 * 定义一些公用的查询
 *
 * @author S.PKAQ
 */
public abstract class ConvertService<M extends BaseMapper, C extends Convert> {
    @Resource
    protected M mapper;

    @Resource
    protected C converter;
}
