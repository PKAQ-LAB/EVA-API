package io.nerv.web.sys.module.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.enums.LockEnumm;
import io.nerv.core.exception.BizException;
import io.nerv.core.mvc.service.mybatis.StdBaseService;
import io.nerv.core.util.tree.TreeHelper;
import io.nerv.web.sys.module.entity.ModuleEntity;
import io.nerv.web.sys.module.entity.ModuleResources;
import io.nerv.web.sys.module.mapper.ModuleMapper;
import io.nerv.web.sys.module.mapper.ModuleResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 模块管理service
 * @author: S.PKAQ
 */
@Service
public class ModuleService extends StdBaseService<ModuleMapper, ModuleEntity> {

    @Autowired
    private ModuleResourceMapper moduleResourceMapper;

    /**
     * 查询模块结构树
     * @return
     */
    public List<ModuleEntity> listModule(ModuleEntity module){
        return this.mapper.listModule(module);
    }

    /**
     * 根据ID批量删除
     * @param ids
     * @return
     */
    public void deleteModule(ArrayList<String> ids){
        // 检查是否存在子节点，存在子节点不允许删除
        QueryWrapper<ModuleEntity> oew = new QueryWrapper<>();
        oew.setEntity( new ModuleEntity() );
        oew.in("parent_ID", ids);

        List<ModuleEntity> leafList = this.mapper.selectList(oew);

        if (CollectionUtil.isNotEmpty(leafList)){
            // 获取存在子节点的节点名称
            List<Object> list = CollectionUtil.getFieldValues(leafList, "parentName");
            // 拼接名称
            String name = CollectionUtil.join(list, ",");
            throw new BizException(BizCodeEnum.CHILD_EXIST.getIndex(), StrUtil.format(BizCodeEnum.CHILD_EXIST.getName(), name));
        } else {
            try{
                // 删除相关资源
                QueryWrapper<ModuleResources> deleteWrapper = new QueryWrapper<>();
                deleteWrapper.in("module_id", ids);
                this.moduleResourceMapper.delete(deleteWrapper);
                // 删除模块
                this.mapper.deleteBatchIds(ids);
            } catch(Exception e){
                throw new BizException(BizCodeEnum.MODULE_RESOURCE_USED);
            }
        }
    }

    /**
     * 新增/编辑一条模块信息
     * @param module 要 新增/编辑 得模块对象
     * @return 重新查询模块列表
     */
    public void editModule(ModuleEntity module){
        String moduleId = module.getId();

        ModuleEntity originModule = null;
        if(StrUtil.isNotBlank(moduleId)){
            originModule = this.getById(moduleId);
        }
        // 获取上级节点
        String pid = module.getParentId();
        if(StrUtil.isNotBlank(moduleId)){
            //是否启用的逻辑
            if(StrUtil.isNotBlank(module.getStatus()) && LockEnumm.UNLOCK.getIndex().equals(module.getStatus())) {
                if(!isDisable(module)){
                    //如果父节点状态为禁用，则子节点状态也只能为禁用
                    throw new BizException(BizCodeEnum.PARENT_NOT_AVAILABLE);
                }
            }
            //是否禁用的逻辑
            disableChild(module);
        }else{
            //新增设置orders为同级模块中最大的orders+1
            module.setIsleaf(true);
            module.setOrders(this.mapper.listOrder(pid)+1);
        }

        String root = "0";
        //  当前编辑节点为子节点
        if(!root.equals(pid) && StrUtil.isNotBlank(pid)){
            // 查询新父节点信息
            ModuleEntity parentModule = this.getModule(pid);
            // 设置当前节点信息
            module.setPathId(StrUtil.isNotBlank(parentModule.getPathId()) ? parentModule.getPathId()+","+parentModule.getId() : parentModule.getId());
            String pathName = StrUtil.format("{}/{}", parentModule.getName(), module.getName()); //pathName

            String oldFatherPath = null;

            if(moduleId != null && originModule != null && StrUtil.isNotBlank(originModule.getParentId())){
                //得到原来父节点的path路径
               ModuleEntity oldParent= this.mapper.selectById(originModule.getParentId());
               oldFatherPath=oldParent != null ? oldParent.getPath() : null;
            }

            module.setPath(TreeHelper.assemblePath(parentModule.getPath(), module.getPath(),oldFatherPath));
            module.setPathName(pathName);
            module.setParentName(parentModule.getName());

        }

        // 判断是否更换了父节点
        // 如果更换了父节点 重新确定原父节点的 leaf属性，以及所修改节点的orders属性
        if(null != originModule && this.parentChanged(originModule.getParentId(), pid)){
            // 更新原节点
            // 检查原父节点是否还存在子节点 来重新确定原始父节点得isleaf属性
            // 由于数据还未提交 节点仍然挂载在原始节点上 所以这里要 -1
            int originParentChilds = this.mapper.countPrantLeaf(originModule.getParentId())-1 ;
            if(originParentChilds < 1){
                ModuleEntity originParentModule = new ModuleEntity();
                originParentModule.setIsleaf(true);
                originParentModule.setId(originModule.getParentId());
                this.mapper.updateById(originParentModule);
            }
            // 更新新节点 isleaf属性
            int newParentChilds = this.mapper.countPrantLeaf(pid) ;
            ModuleEntity newParentModule = new ModuleEntity();
            newParentModule.setIsleaf(false);
            newParentModule.setId(pid);
            this.mapper.updateById(newParentModule);
            // 重新设置节点顺序
            module.setOrders(newParentChilds+1);

        }
        // 持久化
        if (StrUtil.isBlank(moduleId)){
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
        List<ModuleResources> resources = module.getResources();

        if (CollUtil.isNotEmpty(resources)){
            List<String> ids = new ArrayList<>(resources.size());
            for (ModuleResources item : resources) {
                if (StrUtil.isNotBlank(item.getId())){
                    this.moduleResourceMapper.updateById(item);
                } else {
                    item.setId(IdWorker.getIdStr());
                    item.setModuleId(moduleId);
                    this.moduleResourceMapper.insert(item);
                }

                ids.add(item.getId());
            }

            // 移除被删除的
            if (StrUtil.isNotBlank(moduleId)){
                QueryWrapper<ModuleResources> deleteWrapper = new QueryWrapper();
                deleteWrapper.notIn("id", ids);
                deleteWrapper.eq("module_id", moduleId);
                try{
                    this.moduleResourceMapper.delete(deleteWrapper);
                } catch(Exception e){
                    throw new BizException(BizCodeEnum.RESOURCE_USED);
                }
            }
        }


        // 刷新所有子节点的 path parent_name path_name 当修改状态的时候不用刷新子节点信息
        if (StrUtil.isNotBlank(module.getId()) && null != originModule){
            this.refreshChild(module, originModule);
        }

    }

    /**
     * 判断是否更换了父节点
     * @param originPId 原始父节点id
     * @param newPid 新的父节点Id
     * @return
     */
    private boolean parentChanged(String originPId, String newPid){
        originPId = StrUtil.isBlank(originPId)? "0" : originPId;
        newPid = StrUtil.isBlank(newPid)? "0" : newPid;
        return !originPId.equals(newPid);
    }

    // 父节点信息有修改 刷新子节点相关数据
    public void refreshChild(ModuleEntity module,ModuleEntity oldModule){
        // 刷新子节点所有名称
        this.mapper.updateChildParentName(
                module.getPathName(), oldModule.getPathName(),
                module.getPathId(), oldModule.getPathId(),
                module.getName(), module.getId());
    }
    /**
     * 根据ID更新
     */
    public void updateModule(ModuleEntity moduleEntity){
        if(StrUtil.isNotBlank(moduleEntity.getStatus()) && LockEnumm.UNLOCK.getIndex().equals(moduleEntity.getStatus())) {
            if(!isDisable(moduleEntity)){
                return;
            }
        }
        disableChild(moduleEntity);
        this.mapper.updateById(moduleEntity);
    }


    /**
     * 根据ID获取一条模块信息
     * @param id 模块ID
     * @return 模块信息
     */
    public ModuleEntity getModule(String id) {
        ModuleEntity module = this.getById(id);
        // 获取资源信息
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("module_id", id);
        List<ModuleResources> resourceList = this.moduleResourceMapper.selectList(queryWrapper);

        module.setResources(resourceList);

        return module;
    }

    /**
     * 根据属性查询模块树列表
     * @param module 属性实体类
     * @return 模块树列表
     */
    public List<ModuleEntity> listModuleByAttr(ModuleEntity module) {
        //根据名字查询节点信息
        return this.mapper.listModule(module);
    }

    /**
     *  交换两个orders值
     * @param switchModule 进行交换的两个实体
     */
    public void sortModule(ModuleEntity[] switchModule) {
        for (ModuleEntity module : switchModule) {
            this.mapper.updateById(module);
        }
    }
    /**
     * 校验同级节点中path是否唯一
     * @param module
     * @return
     */
    public boolean checkUnique(ModuleEntity module) {
        QueryWrapper<ModuleEntity> entityWrapper = new QueryWrapper<>();
        entityWrapper.eq("path", module.getPath());

        if (StrUtil.isNotBlank(module.getId())){
            entityWrapper.ne("id", module.getId());
        }

        if (StrUtil.isBlank(module.getParentId())){
            entityWrapper.isNull("parent_id");
        } else {
            entityWrapper.eq("parent_id", module.getParentId());
        }

        int records = this.mapper.selectCount(entityWrapper);
        return records > 0;
    }

    /**
     * 父节点被禁用，子节点也会被禁用
     */
    public void disableChild(ModuleEntity module){
        //判断是不是禁用
        if(StrUtil.isBlank(module.getStatus()) || LockEnumm.LOCK.getIndex().equals(module.getStatus())){
            return;
        }
        //禁用该父节点下的所有子节点
        this.mapper.disableChild(module.getId());
    }


    /**
     * 如果父节点状态是禁用 返回false
     * @param moduleEntity
     * @return
     */
    public  boolean isDisable(ModuleEntity moduleEntity){
        ModuleEntity module=this.mapper.selectById(moduleEntity);
        //判断是否启用
            if(StrUtil.isNotBlank(module.getParentId())){
                //得到父节点
                ModuleEntity fatherModule=this.mapper.selectById(module.getParentId());
                if(fatherModule != null && StrUtil.isNotBlank(fatherModule.getStatus())){
                    return LockEnumm.LOCK.getIndex().equals(fatherModule.getStatus()) ? false : true;
                }

            }
        return true;
    }
}
