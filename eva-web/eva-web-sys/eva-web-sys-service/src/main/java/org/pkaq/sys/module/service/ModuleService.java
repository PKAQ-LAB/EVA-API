package org.pkaq.sys.module.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.exception.BizException;
import org.pkaq.core.mvc.bo.SingleArrayBo;
import org.pkaq.core.mybatis.mvc.entity.StdTreeEntity;
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
import org.pkaq.sys.module.vo.ModuleListVo;
import org.pkaq.sys.user.vo.UserResourceVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 模块管理service
 *
 * @author: S.PKAQ
 */
@Service
@RequiredArgsConstructor
public class ModuleService extends StdService<ModuleMapper, ModuleEntity> {
    private final ModuleConvert moduleConvert;
    private final ModuleResourceMapper moduleResourceMapper;

    /**
     * 查询模块结构树
     *
     * @return
     */
    public List<ModuleListVo> listModule(ModuleQueryBo queryBo) {
        return this.mapper.listModule(queryBo);
    }

    /**
     * 根据ID批量删除
     *
     * @param ids
     * @return
     */
    public void deleteModule(ArrayList<String> ids) {
        // 检查是否存在子节点，存在子节点不允许删除
        QueryWrapper<ModuleEntity> oew = new QueryWrapper<>();
        oew.setEntity(new ModuleEntity());
        oew.in("PARENT_ID", ids);

        List<ModuleEntity> leafList = this.mapper.selectList(oew);

        if (CollUtil.isNotEmpty(leafList)) {
            // 获取存在子节点的节点名称
            List<Object> list = CollectionUtil.getFieldValues(leafList, "parentName");
            // 拼接名称
            String name = CollUtil.join(list, ",");

            SysCodes.CHILD_EXIST.newException(name);
        } else {
            try {
                // 删除相关资源
                QueryWrapper<ModuleResources> deleteWrapper = new QueryWrapper<>();
                deleteWrapper.in("MODULE_ID", ids);
                this.moduleResourceMapper.delete(deleteWrapper);
                // 删除模块
                this.mapper.deleteBatchIds(ids);
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
        var module = this.moduleConvert.aoeBoToEntity(bo);
        String moduleId = module.getId();

        ModuleEntity originModule = null;
        if (CharSequenceUtil.isNotBlank(moduleId)) {
            originModule = this.get(moduleId);
        }
        // 获取上级节点
        String pid = module.getPid();
        if (CharSequenceUtil.isNotBlank(moduleId)) {
            //是否启用的逻辑
//            if (CharSequenceUtil.isNotBlank(module.getFrozen()) && FrozenEnumm.UN_FROZEN.getCode().equals(module.getFrozen())) {
//                if (!isDisable(module)) {
//                    //如果父节点状态为禁用，则子节点状态也只能为禁用
//                    throw new BizException(SysCodeEnum.PARENT_NOT_AVAILABLE);
//                }
//            }
            //是否禁用的逻辑
//            disableChild(module);
        } else {
            //新增设置orders为同级模块中最大的orders+1
            module.setIsleaf(true);
            module.setSort(this.mapper.listOrder(pid) + 1);
        }

        String root = "0";
        //  当前编辑节点为子节点
        if (!root.equals(pid) && CharSequenceUtil.isNotBlank(pid)) {
            // 查询新父节点信息
            ModuleDetailVo parentModule = this.getModule(pid);
            // 设置当前节点信息
            module.setPid(CharSequenceUtil.isNotBlank(parentModule.getPath()) ? parentModule.getPath() + "," + parentModule.getId() : parentModule.getId());
            String pathName = CharSequenceUtil.format("{}/{}", parentModule.getName(), module.getName()); //pathName

            String oldFatherPath = null;

            if (moduleId != null && originModule != null && CharSequenceUtil.isNotBlank(originModule.getPid())) {
                //得到原来父节点的path路径
                ModuleEntity oldParent = this.mapper.selectById(originModule.getPid());
                oldFatherPath = oldParent != null ? oldParent.getPath() : null;
            }

            module.setPath(TreeHelper.assemblePath(parentModule.getPath(), module.getPath(), oldFatherPath));

        }

        // 判断是否更换了父节点
        // 如果更换了父节点 重新确定原父节点的 leaf属性，以及所修改节点的orders属性
        if (null != originModule && this.parentChanged(originModule.getPid(), pid)) {
            // 更新原节点
            // 检查原父节点是否还存在子节点 来重新确定原始父节点得isleaf属性
            // 由于数据还未提交 节点仍然挂载在原始节点上 所以这里要 -1
            int originParentChilds = this.mapper.countPrantLeaf(originModule.getPid()) - 1;
            if (originParentChilds < 1) {
                ModuleEntity originParentModule = new ModuleEntity();
                originParentModule.setIsleaf(true);
                originParentModule.setId(originModule.getPid());
                this.mapper.updateById(originParentModule);
            }
            // 更新新节点 isleaf属性
            int newParentChilds = this.mapper.countPrantLeaf(pid);
            ModuleEntity newParentModule = new ModuleEntity();
            newParentModule.setIsleaf(false);
            newParentModule.setId(pid);
            this.mapper.updateById(newParentModule);
            // 重新设置节点顺序
            module.setSort(newParentChilds + 1);

        }
        // 持久化
        if (CharSequenceUtil.isBlank(moduleId)) {
            moduleId = IdWorker.getIdStr();
            module.setId(moduleId);
            this.mapper.insert(module);
        } else {
            this.mapper.updateById(module);
        }

        /**
         写入资源信息, 先删除
         有id 更新
         无id 新增
         */
        List<ModuleResources> resources = bo.getResources();

        if (CollUtil.isNotEmpty(resources)) {
            List<String> ids = new ArrayList<>(resources.size());
            for (ModuleResources item : resources) {
                if (CharSequenceUtil.isNotBlank(item.getId())) {
                    this.moduleResourceMapper.updateById(item);
                } else {
                    item.setId(IdWorker.getIdStr());
                    item.setModuleId(moduleId);
                    this.moduleResourceMapper.insert(item);
                }

                ids.add(item.getId());
            }

            // 移除被删除的
            if (CharSequenceUtil.isNotBlank(moduleId)) {
                QueryWrapper<ModuleResources> deleteWrapper = new QueryWrapper();
                deleteWrapper.notIn("ID", ids);
                deleteWrapper.eq("MODULE_ID", moduleId);
                try {
                    this.moduleResourceMapper.delete(deleteWrapper);
                } catch (Exception e) {
                    throw new BizException(SysCodes.RESOURCE_USED);
                }
            }
        }


        // 刷新所有子节点的 path parent_name path_name 当修改状态的时候不用刷新子节点信息
        if (CharSequenceUtil.isNotBlank(module.getId()) && null != originModule) {
            this.refreshChild(module, originModule);
        }

    }

    /**
     * 判断是否更换了父节点
     *
     * @param originPId 原始父节点id
     * @param newPid    新的父节点Id
     * @return
     */
    private boolean parentChanged(String originPId, String newPid) {
        originPId = CharSequenceUtil.isBlank(originPId) ? "0" : originPId;
        newPid = CharSequenceUtil.isBlank(newPid) ? "0" : newPid;
        return !originPId.equals(newPid);
    }

    // 父节点信息有修改 刷新子节点相关数据
    public void refreshChild(ModuleEntity module, ModuleEntity oldModule) {
        // 刷新子节点所有名称
//        this.mapper.updateChildParentName(
//                module.getPathName(), oldModule.getPathName(),
//                module.getPathId(), oldModule.getPathId(),
//                module.getName(), module.getId());
    }

    /**
     * 根据ID更新
     */
    public void updateModule(ModuleEntity moduleEntity) {
//        if (CharSequenceUtil.isNotBlank(moduleEntity.getFrozen()) && FrozenEnumm.UN_FROZEN.getCode().equals(moduleEntity.getFrozen())) {
//            if (!isDisable(moduleEntity)) {
//                return;
//            }
//        }
//        disableChild(moduleEntity);
        this.mapper.updateById(moduleEntity);
    }


    /**
     * 根据ID获取一条模块信息
     *
     * @param id 模块ID
     * @return 模块信息
     */
    public ModuleDetailVo getModule(String id) {
        ModuleEntity module = this.get(id);
        // 获取资源信息
        LambdaQueryWrapper<ModuleResources> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModuleResources::getModuleId, id);
        List<ModuleResources> resourceList = this.moduleResourceMapper.selectList(queryWrapper);

        var md = this.moduleConvert.entityToDetailVo(module);

        md.setResources(resourceList);

        return md;
    }

    /**
     * 根据属性查询模块树列表
     *
     * @param queryBo 属性实体类
     * @return 模块树列表
     */
    public List<ModuleListVo> listModuleByAttr(ModuleQueryBo queryBo) {
        //根据名字查询节点信息
        return this.mapper.listModule(queryBo);
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
        QueryWrapper<ModuleEntity> entityWrapper = new QueryWrapper<>();
        entityWrapper.eq("PATH", module.getPath());

        if (CharSequenceUtil.isNotBlank(module.getId())) {
            entityWrapper.ne("ID", module.getId());
        }

        if (CharSequenceUtil.isBlank(module.getPid())) {
            entityWrapper.isNull("PARENT_ID");
        } else {
            entityWrapper.eq("PARENT_ID", module.getPid());
        }

        long records = this.mapper.selectCount(entityWrapper);
        return records > 0;
    }

    /**
     * 父节点被禁用，子节点也会被禁用
     */
    public void disableChild(SingleArrayBo<String> ids) {
        //判断是不是禁用
//        if (CharSequenceUtil.isBlank(module.getFrozen()) || FrozenEnumm.FROZEN.getCode().equals(module.getFrozen())) {
//            return;
//        }
//        //禁用该父节点下的所有子节点
//        this.mapper.disableChild(module.getId());
    }


    /**
     * 如果父节点状态是禁用 返回false
     *
     * @param moduleEntity
     * @return
     */
    public boolean isDisable(ModuleEntity moduleEntity) {
        ModuleEntity module = this.mapper.selectById(moduleEntity);
        //判断是否启用
        if (CharSequenceUtil.isNotBlank(module.getPid())) {
            //得到父节点
            ModuleEntity fatherModule = this.mapper.selectById(module.getPid());
//            if (fatherModule != null && CharSequenceUtil.isNotBlank(fatherModule.getFrozen())) {
//                return !FrozenEnumm.FROZEN.getCode().equals(fatherModule.getFrozen());
//            }

        }
        return true;
    }
    /**
     * 获取当前登录用户的信息(菜单.权限.消息
     *
     * @param uid 用户ID
     */
    public List<UserResourceVo> fetchModuleByUid(String uid) {
        // 菜单树
        List<ModuleEntity> moduleEntity = this.mapper.getRoleModuleByUserId(uid);
        List<StdTreeEntity> treeModule = new TreeHelper().bulid(moduleEntity);

        List<UserResourceVo> urv = this.moduleConvert.moduleTreeToUserResourceVo(treeModule);
        // 权限是否为空
        SysCodes.PERMISSION_EXPIRED.assertNotBlank(treeModule);

        return urv;
    }
}
