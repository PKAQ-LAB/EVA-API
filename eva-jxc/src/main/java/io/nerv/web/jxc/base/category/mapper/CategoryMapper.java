package io.nerv.web.jxc.base.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import io.nerv.web.jxc.base.category.entity.CategoryEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author PKAQ
 * @since 2018-11-17
 */
@Mapper
@Repository
public interface CategoryMapper extends BaseMapper<CategoryEntity> {
    /**
     * 查询所有符合条件的树
     * @param condition 包含查询条件的实体类
     * @param category 符合条件的List
     * @return 符合查询条件的List
     */
    List<CategoryEntity> listCategory(@Param("condition") String condition,
                                      @Param("category") CategoryEntity category);

    /**
     * 根据parentID查询子节点数据
     * @param id parentID
     * @return 符合条件的List
     */
    List<CategoryEntity> listChildren(String id);

    /**
     * 根据子节点ID查询父节点信息
     * @param id 子节点ID
     * @return 父节点实体类
     */
    CategoryEntity getParentById(String id);

    /**
     * 根据子节点ID查询同级节点数量（包含自身）
     * @param id 子节点ID
     * @return 同级节点数量
     */
    int countPrantLeaf(String id);
}
