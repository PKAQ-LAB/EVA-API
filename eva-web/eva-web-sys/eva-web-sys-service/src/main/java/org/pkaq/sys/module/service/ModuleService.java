package org.pkaq.sys.module.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.core.mybatis.util.TreeHelper;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.module.bo.ModuleAoeBo;
import org.pkaq.sys.module.bo.ModuleQueryBo;
import org.pkaq.sys.module.bo.ModuleSortBo;
import org.pkaq.sys.module.convert.ModuleConvert;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.module.entity.ModuleResources;
import org.pkaq.sys.module.mapper.ModuleMapper;
import org.pkaq.sys.module.mapper.ModuleResourceMapper;
import org.pkaq.sys.module.vo.ModuleDetailVo;
import org.pkaq.sys.role.mapper.RoleResourceMapper;
import org.pkaq.sys.user.vo.UserResourceVo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 模块管理service
 *
 * @author: S.PKAQ
 */
@Service
@RequiredArgsConstructor
public class ModuleService extends StdService<ModuleMapper, ModuleEntity, ModuleConvert> {
    private final ModuleResourceMapper moduleResourceMapper;

    private final RoleResourceMapper roleResourceMapper;
    /**
     * 根据ID批量删除
     *
     * @param ids
     * @return
     */
    public void deleteModule(List<Long> ids) {
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
            ModuleDetailVo originModule = this.get(moduleId);
            // 1.冻结状态变更逻辑
            if (FrozenEnumm.UN_FROZEN == module.getFrozen() && !isRoot) {
                //解除冻结， 查询其父节点状态是否为冻结，否则不可改变状态
                ModuleEntity parent = this.mapper.selectById(pid);
                if (null != parent && parent.getFrozen() == FrozenEnumm.FROZEN) {
                    throw new BizException(CommonCodes.PARENT_NOT_AVAILABLE);
                }
            } else {
                // 冻结，将节点以及下属子节点一起冻结
                LambdaUpdateWrapper<ModuleEntity> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(ModuleEntity::getId, moduleId).or().eq(ModuleEntity::getPid, pid);
                this.mapper.update(updateWrapper);
            }

            // 2.父节点变更逻辑（重新设置叶子属性，重新调整排序）
            // 原來存在父節點
            if (!Objects.equals(originModule.getId(), pid)) {
                // 判断原有父级是否还有下级节点 没有设置为叶子
                var orginalChildCount = this.mapper.selectCount(new LambdaQueryWrapper<ModuleEntity>().eq(ModuleEntity::getPid, pid));
                if (orginalChildCount - 1 < 1) {
                    this.mapper.update(new LambdaUpdateWrapper<ModuleEntity>()
                            .eq(ModuleEntity::getId, originModule.getPid())
                            .set(ModuleEntity::getIsleaf, false)
                    );
                }
            }
            // 把新的父级设置为非叶子並設置path
            if (!isRoot) {
                this.mapper.update(new LambdaUpdateWrapper<ModuleEntity>()
                        .eq(ModuleEntity::getId, pid)
                        .set(ModuleEntity::getIsleaf, true)
                );

                // 设置id组成的path ： 新的父级节点path 属性 + 其id
                var newParent = this.mapper.selectById(pid);
                var path = CharSequenceUtil.format("{}/{}", newParent.getPath(), moduleId);

                module.setPath(path);
            } else {
                module.setPath(null);
            }

            // 父节点变更导致path变更，更新该节点下所有得子级path
            // TODO
        }

        // 更新資源信息（注意：存在权限引用，不可使用先删除再写入方案）
        // 更新资源信息
        LocalDateTime utc_now = LocalDateTime.now();
        if (CollUtil.isNotEmpty(bo.getResources())) {
            var resource = this.convert.resourceBoToEntity(bo.getResources());
            resource.forEach(this.moduleResourceMapper::insertOrUpdate);
        }
        // 删除小于更新时间的
        this.moduleResourceMapper.delete(new LambdaUpdateWrapper<ModuleResources>()
                .eq(ModuleResources::getMainId, moduleId)
                .lt(ModuleResources::getUtcModify, utc_now));
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
        ModuleDetailVo md = this.get(id);

        // 获取资源信息
        LambdaQueryWrapper<ModuleResources> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModuleResources::getMainId, id);

        var resourceList = this.convert.resourceEntityToVo(this.moduleResourceMapper.selectList(queryWrapper));
        md.setResources(resourceList);

        // 树形结构转换
        return md;
    }

    /**
     * 根据属性查询模块树列表（含资源）
     *
     * @param queryBo 属性实体类
     * @return 模块树列表
     */
    public Collection<ModuleDetailVo> list(ModuleQueryBo queryBo) {
        // 根据条件查询模块，返回Map<moduleId, ModuleDetailVo>
        Map<Long, ModuleDetailVo> moduleMap = this.mapper.selectModuleMapList(queryBo);

        if (CollUtil.isEmpty(moduleMap)) {
            return Collections.emptyList();
        }

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

        // 构造树形结构返回
        return TreeHelper.buildTree(moduleMap.values());
    }


    /**
     * 交换两个orders值
     *
     * @param switchModule 进行交换的两个实体
     */
    public void sortModule(ModuleSortBo[] switchModule) {
//        for (ModuleEntity module : switchModule) {
//            this.mapper.updateById(module);
//        }
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
     * 获取当前登录用户的信息(菜单.权限.消息
     *
     * @param uid 用户ID
     */
    public List<UserResourceVo> fetchModuleByUid(Long uid) {
//        // 菜单树
//        List<ModuleEntity> moduleEntity = this.mapper.getRoleModuleByUserId(uid);
//        List<StdTreeEntity> treeModule = TreeHelper().bulid(moduleEntity);
//
//        List<UserResourceVo> urv = this.convert.moduleTreeToUserResourceVo(treeModule);
//        // 权限是否为空
////        SysCodes.PERMISSION_EXPIRED.assertNotBlank(treeModule);
//
//        return urv;
        return null;
    }
}
