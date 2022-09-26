package io.nerv.core.mvc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字典管理mapper
 * @author S.PKAQ
 */
@Mapper
@Repository
public interface StdMultiMapper<T> extends BaseMapper<T> {
    /**
     * 查询所有符合条件
     * @return 符合查询条件的List
     */
    List<T> list(T entity);

    /**
     * 查询子表数据
     * @param mainId
     * @return
     */
    List<T> listLines(String mainId);
}