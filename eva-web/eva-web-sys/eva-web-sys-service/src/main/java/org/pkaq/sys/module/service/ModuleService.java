package org.pkaq.sys.module.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.util.TreeHelper;
import org.pkaq.core.util.StrUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.module.bo.ModuleAoeBo;
import org.pkaq.sys.module.bo.ModuleQueryBo;
import org.pkaq.sys.module.bo.ModuleResourcesBo;
import org.pkaq.sys.module.bo.ModuleSortBo;
import org.pkaq.sys.module.convert.ModuleConvert;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.entity.ModuleResources;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.module.mapper.ModuleResourceMapper;
import org.pkaq.sys.module.vo.ModuleDetailVo;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 模块管理service
 *
 * @author: S.PKAQ
 */
@Service
@RequiredArgsConstructor
public class ModuleService extends StdService<ModuleMapper, ModuleEntity> {
    private final ModuleResourceMapper moduleResourceMapper;

    private final RoleResourceMapper roleResourceMapper;

    private final ModuleConvert convert;


    /**
     * 根据ID批量删除
     *
     * @param ids
     * @return
     */
    public void deleteModule(Set<Long> ids) {
        // 检查是否存在子节点，存在子节点不允许删除
        LambdaQueryWrapper<ModuleEntity> oew = new LambdaQueryWrapper<>();
        oew.in(ModuleEntity::getPid, ids);

        List<ModuleEntity> leafList = this.mapper.selectList(oew);

        if (CollUtil.isNotEmpty(leafList)) {
            // 获取存在子节点的节点名称
            String nameStr = leafList.stream()
                    .map(ModuleEntity::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));

            CommonCodes.CHILD_EXIST.newException(nameStr);
        } else {
            try {
                // 删除相关资源
                LambdaUpdateWrapper<ModuleResources> deleteWrapper = new LambdaUpdateWrapper<>();
                deleteWrapper.in(ModuleResources::getMainId, ids);
                this.moduleResourceMapper.delete(deleteWrapper);
                // 删除模块
                this.mapper.deleteByIds(ids);
                // 删除授权
                this.roleResourceMapper.deleteByModuleIds(ids);
            } catch (Exception e) {
                throw new BizException(SysCodes.MODULE_RESOURCE_USED);
            }
        }
    }

    /**
     * 新增/编辑一条模块信息
     *
     * @param bo 要 新增/编辑 得模块对象
     * @return 重新查询模块列表
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void editModule(ModuleAoeBo bo) {
        var module = this.convert.boToEntity(bo);

        Long moduleId = module.getId();
        Long pid = module.getPid();
        var isNew = moduleId == null || moduleId == 0;
        var isRoot = pid == null || pid == 0;

        if (isNew) {
            module.setIsleaf(true);
            this.mapper.insert(module);
        } else {
            // 树原有信息
            ModuleDetailVo originModule = this.convert.entityToDetailVo(this.get(moduleId));
            // 处理冻结逻辑
            this.handleFrozenStatus(module, originModule, isRoot);
            // 2.父节点变更逻辑（重新设置叶子属性，重新调整排序）
            this.handleParentChange(module, originModule, isRoot);
        }

        // 更新資源信息（注意：存在权限引用，不可使用先删除再写入方案）
        this.handleResources(moduleId, bo.getResources());
        // 删除权限中失效的的引用关系
        this.roleResourceMapper.purgeBrokenRoleResourceRefs(moduleId);
    }

    /**
     * 根据ID获取一条模块信息
     *
     * @param id 模块ID
     * @return 模块信息
     */
    public ModuleDetailVo getModule(Long id) {
        ModuleDetailVo md = this.convert.entityToDetailVo(this.get(id));

        // 获取资源信息
        LambdaQueryWrapper<ModuleResources> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModuleResources::getMainId, id);

        var resourceList = this.convert.resourceEntityToVo(this.moduleResourceMapper.selectList(queryWrapper));
        md.setResources(resourceList);

        return md;
    }

    /**
     * 根据属性查询模块树列表（含资源）
     *
     * @param queryBo 属性实体类
     * @return 模块树列表
     */
    public Collection<ModuleDetailVo> list(ModuleQueryBo queryBo, boolean withResource) {
        // 根据条件查询模块，返回Map<moduleId, ModuleDetailVo>
        Map<Long, ModuleDetailVo> moduleMap = this.mapper.selectModuleMapList(queryBo);

        if (CollUtil.isEmpty(moduleMap)) {
            return Collections.emptyList();
        }
        if (withResource) {
            this.handleFetchResource(moduleMap);
        }

        // 构造树形结构返回
        return TreeHelper.buildTree(moduleMap.values());
    }

    /**
     * 交换两个orders值
     *
     * @param bo 进行交换的两个实体
     */
    public void sortModule(ModuleSortBo bo) {
        this.mapper.updateSort(bo.getId(), bo.getOldSort(), bo.getNewSort());
    }

    /**
     * 校验同级节点中path是否唯一
     *
     * @param module
     * @return
     */
    public boolean checkUnique(ModuleEntity module) {
        LambdaQueryWrapper<ModuleEntity> entityWrapper = new LambdaQueryWrapper<>();
        entityWrapper.eq(ModuleEntity::getPath, module.getPath());
        if (null != module.getPid() && 0 != module.getPid()) {
            entityWrapper.ne(ModuleEntity::getId, module.getId());
        }

        if (null == module.getPid() || 0 == module.getPid()) {
            entityWrapper.isNull(ModuleEntity::getPid);
        } else {
            entityWrapper.eq(ModuleEntity::getPid, module.getPid());
        }

        long records = this.mapper.selectCount(entityWrapper);
        return records > 0;
    }

    /**
     *
     * @param moduleMap
     */
    private void handleFetchResource(Map<Long, ModuleDetailVo> moduleMap) {
        // 批量查询模块对应的资源
        LambdaQueryWrapper<ModuleResources> resourceQuery = new LambdaQueryWrapper<>();
        resourceQuery.in(ModuleResources::getMainId, moduleMap.keySet());
        List<ModuleResources> resourceList = this.moduleResourceMapper.selectList(resourceQuery);

        // 按模块ID分组资源，转换并设置到对应模块Vo中
        Map<Long, List<ModuleResources>> resourceGroupByModule = resourceList.stream()
                .collect(Collectors.groupingBy(ModuleResources::getMainId));

        resourceGroupByModule.forEach((moduleId, resources) -> {
            ModuleDetailVo moduleVo = moduleMap.get(moduleId);
            if (moduleVo != null) {
                moduleVo.setResources(this.convert.resourceEntityToVo(resources));
            }
        });
    }

    /**
     * 调整冻结状态
     *
     * @param module
     * @param originModule
     * @param isRoot
     */
    private void handleFrozenStatus(ModuleEntity module, ModuleDetailVo originModule, boolean isRoot) {
        if (FrozenEnumm.UN_FROZEN == module.getFrozen()) {
            // 解除冻结
            if (!isRoot) {
                ModuleEntity parent = this.mapper.selectById(module.getPid());
                if (parent != null && parent.getFrozen() == FrozenEnumm.FROZEN) {
                    throw new BizException(CommonCodes.PARENT_NOT_AVAILABLE);
                }
            }
            this.mapper.update(new LambdaUpdateWrapper<ModuleEntity>()
                    .likeRight(ModuleEntity::getPath, originModule.getPath())
                    .set(ModuleEntity::getFrozen, FrozenEnumm.UN_FROZEN));
        } else {
            // 冻结节点及子节点
            this.mapper.update(new LambdaUpdateWrapper<ModuleEntity>()
                    .likeRight(ModuleEntity::getPath, originModule.getPath())
                    .set(ModuleEntity::getFrozen, FrozenEnumm.FROZEN));
        }
    }

    /**
     * 父节点变更处理
     *
     * @param module
     * @param originModule
     * @param isRoot
     */
    private void handleParentChange(ModuleEntity module, ModuleDetailVo originModule, boolean isRoot) {
        Long moduleId = module.getId();
        Long pid = module.getPid();
        // 原來存在父節點
        if (!Objects.equals(originModule.getPid(), pid)) {
            // 判断原有父级是否还有下级节点 没有设置为叶子
            var orginalChildCount = mapper.selectCount(new LambdaQueryWrapper<ModuleEntity>()
                    .eq(ModuleEntity::getPid, originModule.getPid())
                    .ne(ModuleEntity::getId, moduleId)
            );
            // 原父节点变为叶子
            if (orginalChildCount == 0) {
                this.mapper.update(new LambdaUpdateWrapper<ModuleEntity>()
                        .eq(ModuleEntity::getId, originModule.getPid())
                        .set(ModuleEntity::getIsleaf, true)
                );
            }
            // 新父节点变为非叶子
            if (!isRoot) {
                mapper.update(null, new LambdaUpdateWrapper<ModuleEntity>()
                        .eq(ModuleEntity::getId, pid)
                        .set(ModuleEntity::getIsleaf, false)
                );
            }
            // 更新path
            if (!isRoot) {
                // 设置id组成的path ： 新的父级节点path 属性 + 其id
                var newParent = this.mapper.selectById(pid);
                var newPath = String.format("%s/%s", newParent.getPath(), moduleId);;
                module.setPath(newPath);

                mapper.updateById(module);
                var oldPath = originModule.getPath();
                if (StrUtils.isNotEmpty(oldPath)) {
                    this.mapper.refreshPath(oldPath, oldPath.length(), newPath);
                }
            } else {
                module.setPath(null);
                mapper.updateById(module);
            }
        }
    }

    /**
     * 资源更新
     *
     * @param moduleId
     * @param resources
     */
    private void handleResources(Long moduleId, List<ModuleResourcesBo> resources) {
        long batchId = IdUtil.getSnowflakeNextId();
        // 更新资源信息
        if (CollUtil.isNotEmpty(resources)) {
            var resource = this.convert.resourceBoToEntity(resources);
            resource.forEach(r -> {
                r.setBatchId(batchId);
                this.moduleResourceMapper.insertOrUpdate(r);
            });
        }
        // 删除小于更新时间的
        this.moduleResourceMapper.delete(new LambdaUpdateWrapper<ModuleResources>()
                .eq(ModuleResources::getMainId, moduleId)
                .ne(ModuleResources::getBatchId, batchId));
    }

    /**
     * 动态切换树的冻结状态
     *
     * @param ids
     */
    public void switchFrozen(SingleArray<Long> ids) {
        for (Long id : ids.getParam()) {
            ModuleEntity parent = mapper.selectOne(new LambdaQueryWrapper<ModuleEntity>().eq(ModuleEntity::getPid, id));
            // 父节点冻结，禁止切换（严格模式）
            if (parent.getPid() != null && 0 != parent.getPid() && parent.getFrozen() == FrozenEnumm.FROZEN) {
                continue;
            }
            // 级联切换节点状态
            this.mapper.switchFrozen(ids);
        }
    }
}
