package org.pkaq.core.mybatis.mvc.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.mvc.bo.Bo;
import org.pkaq.core.mvc.bo.IdCodeBo;
import org.pkaq.core.mvc.bo.PageBo;
import org.pkaq.core.mvc.convert.Convert;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mvc.vo.Vo;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;
import org.pkaq.core.mybatis.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * service 基类
 * 抛出exception异常时 回滚事务
 * 定义一些公用的查询
 *
 * @author S.PKAQ
 */
public abstract class StdService<M extends BaseMapper<T>, T extends StdEntity, C extends Convert<T>> {
    @Autowired
    protected M mapper;

    @Autowired
    protected C convert;

    protected abstract Convert<T> getConvert();

    public boolean isUnique(IdCodeBo idCodeBo){
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.eq("code", idCodeBo.getCode());

        if (null != idCodeBo.getId() && 0 != idCodeBo.getId()) {
            wrapper.ne("id", idCodeBo.getId());
        }
        return this.mapper.selectCount(wrapper) > 0;
    }
    /**
     * 通用根据ID查询
     */
    @BizLog(operateType = BizLogCodes.QUERY, description = "根据id查询")
    public <V extends Vo> V get(long id) {
        var entity = this.mapper.selectById(id);
        if (entity == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(id);
        }
        return this.getConvert().entityToDetailVo(entity);
    }

    /**
     * 查询符合条件得记录条数
     */
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询了的记录条数")
    protected Long count(Bo bo) {
        var entity = this.getConvert().boToEntity(bo);
        Wrapper<T> wrapper = Wrappers.lambdaQuery(entity);
        return this.mapper.selectCount(wrapper);
    }

    /**
     * 根据条件获取一条记录
     */
    @BizLog(operateType = BizLogCodes.QUERY, description = "根据条件获取一条记录")
    protected Vo get(Bo bo) {
        var entity = this.getConvert().boToEntity(bo);
        Wrapper<T> wrapper = Wrappers.lambdaQuery(entity);
        var e = this.mapper.selectOne(wrapper);
        if (e == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(entity.getId());
        }
        return this.convert.entityToDetailVo(entity);
    }

    /**
     * 合并保存,如果不存在id执行插入,存在ID执行更新
     */
    @BizLog(operateType = BizLogCodes.EDIT, description = "保存记录[{0}]", args = {"param:0.id"})
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void merge(Bo bo) {
        var entity = this.getConvert().boToEntity(bo);
        this.mapper.insertOrUpdate(entity);
    }

    /**
     * 查询所有
     */
    @BizLog(operateType = BizLogCodes.QUERY, description = "根据条件查询记录")
    public List<? extends Vo> list(Bo bo) {
        var entity = this.getConvert().boToEntity(bo);

        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();
        wrapper.orderByDesc(T::getModifyBy);
        wrapper.setEntity(entity);

        return this.getConvert().entityToListVo(this.mapper.selectList(wrapper));
    }

    /**
     * 按分页查询
     */
    @BizLog(operateType = BizLogCodes.QUERY, description = "分页查询记录")
    public PageVo<Vo> listPage(PageBo pageBo) {
        var entity = this.getConvert().boToEntity(pageBo);

        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();
        wrapper.setEntity(entity);
        wrapper.orderByDesc(T::getUtcModify);

        PageResult<T> pagination = new PageResult<>(pageBo.getPageNo(), pageBo.getPageSize());
        return this.mapper.selectPage(pagination, wrapper).map(this.getConvert()::entityToListVo);
    }

    /**
     * 通用删除
     */
    @BizLog(operateType = BizLogCodes.DELETE, description = "删除记录[{0}]", args = {"param:0"})
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void delete(List<Long> param) {
        this.mapper.deleteByIds(param);
    }
}
