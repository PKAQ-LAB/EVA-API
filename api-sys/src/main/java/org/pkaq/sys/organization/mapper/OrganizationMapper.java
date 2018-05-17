package org.pkaq.sys.organization.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.sys.organization.entity.OrganizationEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 组织管理mapper
 * @author PKAQ
 */
@Mapper
@Repository
public interface OrganizationMapper extends BaseMapper<OrganizationEntity>{
    /**
     * 查询所有符合条件的树
     * @param condition 包含查询条件的实体类
     * @param organization 符合条件的List
     * @return 符合查询条件的List
     */
    List<OrganizationEntity> listOrg(@Param("condition") String condition,
                                     @Param("organization") OrganizationEntity organization);

    /**
     * 根据parentID查询子节点数据
     * @param id parentID
     * @return 符合条件的List
     */
    List<OrganizationEntity> listChildren(String id);

    /**
     * 根据子节点ID查询父节点信息
     * @param id 子节点ID
     * @return 父节点实体类
     */
    OrganizationEntity getParentById(String id);

    /**
     * 根据子节点ID查询同级节点数量（包含自身）
     * @param id 子节点ID
     * @return 同级节点数量
     */
    int countPrantLeaf(String id);

    /**
     * 切换可用状态
     * @param organization
     */
    void switchStatus(OrganizationEntity organization);
}