package org.pkaq.web.jxc.base.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.pkaq.web.jxc.base.category.entity.CategoryEntity;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author PKAQ
 * @since 2018-11-17
 */
@Mapper
@Repository
public interface CategoryMapper extends BaseMapper<CategoryEntity> {

}
