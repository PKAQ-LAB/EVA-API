package org.pkaq.web.sys.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.web.sys.module.entity.ModuleEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 模块管理module
 * @author: S.PKAQ
 * @Datetime: 2018/3/28 19:17
 */
@Mapper
@Repository
public interface ModuleMapper extends BaseMapper<ModuleEntity> {
    /**
     * 查询所有符合条件的树
     * @param condition 包含查询条件的实体类
     * @param module 符合条件的List
     * @return 符合查询条件的List
     */
    List<ModuleEntity> listModule(@Param("condition") String condition,
                                  @Param("module") ModuleEntity module);

    /**
     * 根据parentID查询子节点数据
     * @param id parentID
     * @return 符合条件的List
     */
    List<ModuleEntity> listChildren(String id);

    /**
     * 根据用户id查询用户拥有的权限模块列表
     * @param userId 用户id
     * @return 符合条件的List
     */
    List<ModuleEntity> getRoleModuleByUserId(String userId);

    /**
     * 根据子节点ID查询父节点信息
     * @param id 子节点ID
     * @return 父节点实体类
     */
    ModuleEntity getParentById(String id);

    /**
     * 根据子节点ID查询同级节点数量（包含自身）
     * @param id 子节点ID
     * @return 同级节点数量
     */
    int countPrantLeaf(String id);
}