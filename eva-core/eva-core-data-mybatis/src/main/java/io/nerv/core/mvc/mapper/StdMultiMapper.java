package io.nerv.core.mvc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 字典管理mapper
 * @author S.PKAQ
 */
@Mapper
@Repository
public interface StdMultiMapper<T> extends BaseMapper<T> {

    /**
     * 根据id查询
     * @param id
     * @return
     */
    T getById(String id);

    /**
     *根据条件查询一条
     * @param entity
     * @return
     */
    T get(T entity);
}