package io.nerv.sys.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.nerv.core.annotation.Ignore;
import io.nerv.sys.module.entity.ModuleEntityStd;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 模块管理module
 *
 * @author: S.PKAQ
 */
@Mapper
@Repository
public interface ModuleMapper extends BaseMapper<ModuleEntityStd> {


    /**
     * 查询用户拥有权限的模块树
     *
     * @param condition
     * @param module
     * @param roleNames
     * @return
     */
    @Ignore
    List<ModuleEntityStd> listGrantedModule(@Param("condition") String condition,
                                            @Param("module") ModuleEntityStd module,
                                            @Param("roleNames") String[] roleNames);

    /**
     * 查询所有符合条件的树
     *
     * @param module 符合条件的List
     * @return 符合查询条件的List
     */
    List<ModuleEntityStd> listModule(@Param("module") ModuleEntityStd module);

    /**
     * 根据parentID查询子节点数据
     *
     * @param id parentID
     * @return 符合条件的List
     */
    List<ModuleEntityStd> listChildren(String id);

    /**
     * 根据用户id查询用户拥有的权限模块列表
     *
     * @param userId 用户id
     * @return 符合条件的List
     */
    @Ignore
    List<ModuleEntityStd> getRoleModuleByUserId(String userId);

    /**
     * 根据子节点ID查询父节点信息
     *
     * @param id 子节点ID
     * @return 父节点实体类
     */
    ModuleEntityStd getParentById(String id);

    /**
     * 根据子节点ID查询同级节点数量（包含自身）
     *
     * @param id 子节点ID
     * @return 同级节点数量
     */
    int countPrantLeaf(String id);


    /**
     * 根据parentID查询最大的排序值
     *
     * @param id parentID
     */
    Integer listOrder(@Param("pid") String id);

    /**
     * 根据id禁用子节点
     */
    void disableChild(@Param("id") String id);

    ModuleEntityStd selectId(@Param("id") String id);

    /**
     * 刷新子节点名称
     */
    void updateChildParentName(@Param("newPathName") String newPathName, @Param("oldPathName") String oldPathName,
                               @Param("newPathId") String newPathId, @Param("oldPathId") String oldPathId,
                               @Param("name") String name, @Param("id") String id);


}
