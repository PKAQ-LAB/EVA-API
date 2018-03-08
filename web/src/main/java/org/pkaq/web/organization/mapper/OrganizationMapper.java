package org.pkaq.web.organization.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.web.organization.entity.OrganizationEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 组织管理mapper
 */
@Mapper
@Repository
public interface OrganizationMapper extends BaseMapper<OrganizationEntity>{
    /**
     * 查询所有符合条件的树
     * @param entity 包含查询条件的实体类
     * @return 符合查询条件的List
     */
    List<OrganizationEntity> listOrg(@Param("condition") String condition);

    /**
     * 根据parentID查询子节点数据
     * @param id parentID
     * @return 符合条件的List
     */
    List<OrganizationEntity> listChildren(String id);
}