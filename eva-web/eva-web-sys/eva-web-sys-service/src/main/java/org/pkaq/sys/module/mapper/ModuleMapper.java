package org.pkaq.sys.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.pkaq.core.annotation.Ignore;
import org.pkaq.sys.module.bo.ModuleQueryBo;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.vo.ModuleDetailVo;
import org.pkaq.sys.module.vo.ModuleListVo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 模块管理module
 *
 * @author PKAQ
 */
@Mapper
@Repository
public interface ModuleMapper extends BaseMapper<ModuleEntity> {


    /**
     * 查询用户拥有权限的模块树
     *
     * @param condition
     * @param module
     * @param roleNames
     * @return
     */
    @Ignore
    List<ModuleEntity> listGrantedModule(@Param("condition") String condition,
                                         @Param("module") ModuleEntity module,
                                         @Param("roleNames") String[] roleNames);

    /**
     * 查询所有符合条件的树
     *
     * @param module 符合条件的List
     * @return 符合查询条件的List
     */
    List<ModuleListVo> listModule(@Param("module") ModuleQueryBo module);

    /**
     * 根据parentID查询子节点数据
     *
     * @param id parentID
     * @return 符合条件的List
     */
    List<ModuleEntity> listChildren(Long id);

    /**
     * 根据用户id查询用户拥有的权限模块列表
     *
     * @param userId 用户id
     * @return 符合条件的List
     */
    @Ignore
    List<ModuleEntity> getRoleModuleByUserId(Long userId);

    /**
     * 根据子节点ID查询父节点信息
     *
     * @param id 子节点ID
     * @return 父节点实体类
     */
    ModuleEntity getParentById(Long id);

    /**
     * 根据子节点ID查询同级节点数量（包含自身）
     *
     * @param id 子节点ID
     * @return 同级节点数量
     */
    int countPrantLeaf(Long id);


    /**
     * 根据parentID查询最大的排序值
     *
     * @param id parentID
     */
    Integer listOrder(@Param("pid") Long id);

    /**
     * 根据id禁用子节点
     */
    void disableChild(@Param("id") Long id);

    ModuleEntity selectId(@Param("id") Long id);

    /**
     * 刷新子节点名称
     */
    void updateChildParentName(@Param("newPathName") String newPathName, @Param("oldPathName") String oldPathName,
                               @Param("newPathId") String newPathId, @Param("oldPathId") String oldPathId,
                               @Param("name") String name, @Param("id") String id);

    /**
     * 查询当前用户拥有得所有角色包含的资源合集
     *
     * @param uid
     * @return
     */
    @MapKey("id")
    Map<Long, ModuleDetailVo> listGrantedModules(long uid);

    @MapKey("id")
    Map<Long, ModuleDetailVo> selectModuleMapList(@Param("bo") ModuleQueryBo bo);

    /**
     * 刷新树的path
     *
     * @param oldPath
     * @param oldPathLength
     * @param newPath
     */
    void refreshPath(@Param("oldPath") String oldPath,
                     @Param("oldPathLength") int oldPathLength,
                     @Param("newPath") String newPath);

    /**
     * 更新节点排序（仅在同一父节点下生效）
     *
     * @param id      被拖拽的节点 id
     * @param pid     该节点的父 id；根节点之间排序时传 0
     * @param oldSort 拖拽前的 sort 值
     * @param newSort 拖拽后的 sort 值
     */
    void updateSort(@Param("id") Long id,
                    @Param("pid") Long pid,
                    @Param("oldSort") Integer oldSort,
                    @Param("newSort") Integer newSort);

    /**
     * 级联冻结/解锁：自身 + 所有子孙
     *
     * @param id     被操作的节点 id
     * @param path   该节点的 path（用于锚定子孙节点）
     * @param frozen 目标 frozen 值（0 = 解锁，1 = 冻结）
     */
    void cascadeFrozen(@Param("id") Long id,
                       @Param("path") String path,
                       @Param("frozen") Integer frozen);
}
