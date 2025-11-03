package org.pkaq.core.mvc.convert;


import org.pkaq.core.mvc.bo.Bo;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.core.mvc.vo.Vo;

/**
 * @author PKAQ
 */
public abstract class Convert<T> {
    public T fromBo(Bo bo) {
        if (bo == null) return null;
        return dispatchBoToEntity(bo);
    }

    public <V extends Vo> V toVo(T entity) {
        if (entity == null) return null;
        return dispatchEntityToVo(entity);
    }

    public PageVo toPageVo(Object entity) {
        if (entity == null) return null;
        return dispatchEntityToPageVo(entity);
    }

    /**
     * 子类必须实现：根据 Bo 实际类型分派转换逻辑
     */
    protected abstract T dispatchBoToEntity(Bo bo);

    /**
     * 子类必须实现：根据场景或类型返回不同的 Vo
     */
    protected abstract <V extends Vo> V dispatchEntityToVo(T entity);

    protected abstract PageVo dispatchEntityToPageVo(Object entity);
}