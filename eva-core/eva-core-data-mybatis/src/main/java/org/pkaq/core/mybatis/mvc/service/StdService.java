package org.pkaq.core.mybatis.mvc.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mvc.bo.*;
import org.pkaq.core.mvc.convert.Convert;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mvc.vo.Vo;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;
import org.pkaq.core.mybatis.util.PageResult;
import org.pkaq.core.util.CollUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;


/**
 * service 基类
 * 抛出exception异常时 回滚事务
 * 定义一些公用的查询
 *
 * @author S.PKAQ
 */
public abstract class StdService<M extends BaseMapper<T>, T extends StdEntity> {
    @Autowired
    protected M mapper;

    @Autowired(required = false)
    protected Convert convert;

    /**
     * 切换锁定状态
     *
     * @param ids
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void switchFrozen(SingleArray<Long> ids) {
        if (null == ids || ids.getParam() == null) {
            CommonCodes.PARAM_ERROR.newException();
        }

        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper
                .in("id", Objects.requireNonNull(ids).getParam())
                .setSql("frozen = abs(frozen - 1)")
                .ne("frozen", -1);

        this.mapper.update(updateWrapper);
    }

    /**
     * 校验编码唯一性
     */
    public boolean isUnique(IdCodeBo idCodeBo) {
        if (null == idCodeBo || idCodeBo.getId() == null) {
            CommonCodes.PARAM_ERROR.newException();
        }

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
    public <V extends Vo> V get(Long id) {
        if (0 == id) {
            CommonCodes.PARAM_ERROR.newException();
        }

        var entity = this.mapper.selectById(id);
        if (entity == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(id);
        }

        return this.convert.toVo(entity);
    }

    /**
     * 根据条件获取一条记录
     */

    protected T get(T entity) {
        if (null == entity) {
            CommonCodes.PARAM_ERROR.newException();
        }

        Wrapper<T> wrapper = Wrappers.lambdaQuery(entity);
        var e = this.mapper.selectOne(wrapper);
        if (e == null) {
            CommonCodes.CAN_NOT_FIND_RECORD.newException(entity.getId());
        }
        return e;
    }

    /**
     * 查询符合条件得记录条数
     */
    protected Long count(T entity) {
        Wrapper<T> wrapper = Wrappers.lambdaQuery(entity);
        return this.mapper.selectCount(wrapper);
    }


    /**
     * 合并保存,如果不存在id执行插入,存在ID执行更新
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    protected void merge(T entity) {
        if (null == entity) {
            CommonCodes.PARAM_ERROR.newException();
        }
        this.mapper.insertOrUpdate(entity);
    }

    /**
     * 新增/编辑一条信息
     */

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void edit(Bo bo) {
        if (null == bo) {
            CommonCodes.PARAM_ERROR.newException();
        }
        T entity = this.convert.fromBo(bo);
        this.mapper.insertOrUpdate(entity);
    }

    /**
     * 新增/编辑一条信息,校验code唯一性
     */

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void editUniqueCode(StdBo bo) {
        if (null == bo) {
            CommonCodes.PARAM_ERROR.newException();
        }

        var codeCheck = new IdCodeBo();
        codeCheck.setCode(Objects.requireNonNull(bo).getCode());
        codeCheck.setId(bo.getId());


        if (this.isUnique(codeCheck)) {
            CommonCodes.DUPLICATE_CODE_ERROR.newException();
        }

        T entity = this.convert.fromBo(bo);
        this.mapper.insertOrUpdate(entity);
    }

    /**
     * 查询所有
     */
    public List<T> list(T entity) {
        if (null == entity) {
            CommonCodes.PARAM_ERROR.newException();
        }

        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();
        wrapper.orderByDesc(T::getModifyBy);
        wrapper.setEntity(entity);

        return this.mapper.selectList(wrapper);
    }


    /**
     * 按分页查询
     */
    public <P extends PageBo> PageVo listPage(P page) {
        if (null == page) {
            CommonCodes.PARAM_ERROR.newException();
        }

        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();
        T entity = this.convert.fromBo(page);

        wrapper.setEntity(entity);
        wrapper.orderByDesc(T::getUtcModify);

        PageResult<T> pagination = new PageResult<>(page.getPageNo(), page.getPageSize());
        return this.mapper.selectPage(pagination, wrapper).map(this.convert::toVo) ;
    }


    /**
     * 按分页查询
     */
    public PageResult<T> listPage(T entity, int pageNo, int pageSize) {
        if (null == entity) {
            CommonCodes.PARAM_ERROR.newException();
        }


        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();
        wrapper.setEntity(entity);
        wrapper.orderByDesc(T::getUtcModify);

        PageResult<T> pagination = new PageResult<>(pageNo, pageSize);
        return this.mapper.selectPage(pagination, wrapper);
    }

    /**
     * 通用删除
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void delete(Set<Long> param) {
        if (CollUtils.isNotEmpty(param)) {
            CommonCodes.PARAM_ERROR.newException();
        }

        this.mapper.deleteByIds(param);
    }
}