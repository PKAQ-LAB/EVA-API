package io.nerv.web.sys.module.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.nerv.core.mvc.service.BaseService;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.util.tree.TreeHelper;
import io.nerv.web.sys.module.entity.ModuleEntity;
import io.nerv.web.sys.module.mapper.ModuleMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 模块管理service
 * @author: S.PKAQ
 * @Datetime: 2018/3/28 19:20
 */
@Service
public class ModuleService extends BaseService<ModuleMapper, ModuleEntity> {
    /**
     * 查询模块结构树
     * @return
     */
    public List<ModuleEntity> listModule(String condition){
        return this.mapper.listModule(condition, null);
    }

    /**
     * 根据ID批量删除
     * @param ids
     * @return
     */
    public Response deleteModule(ArrayList<String> ids){
        Response response = new Response();
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
            response = response.failure(501, StrUtil.format("[{}] 存在子节点，无法删除。",name), null);
        } else {
            this.mapper.deleteBatchIds(ids);
            response = response.success(this.mapper.listModule(null, null));
        }

        return response;
    }
    /**
     * 新增/编辑一条模块信息
     * @param module 要 新增/编辑 得模块对象
     * @return 重新查询模块列表
     */
    public List<ModuleEntity> editModule(ModuleEntity module){
        String moduleId = module.getId();
        // 获取上级节点
        String pid = module.getParentId();
        String root = "0";
        if(!root.equals(pid) && StrUtil.isNotBlank(pid)){
            // 查询新父节点信息
            ModuleEntity parentModule = this.getModule(pid);
            // 设置当前节点信息
            module.setPathId(parentModule.getId());
            String pathName = StrUtil.format("{}/{}", parentModule.getName(), module.getName());

            module.setPath(TreeHelper.assemblePath(parentModule.getPath(), module.getPath()));
            module.setPathName(pathName);
            module.setParentName(parentModule.getName());

        } else {
            // 父节点为空, 根节点 设置为非叶子
            module.setIsleaf(false);
            module.setPathId(module.getId());
            module.setParentName(module.getName());
        }

        // 检查原父节点是否还存在子节点 不存在设置leaf为false
        ModuleEntity moduleinNode = this.mapper.getParentById(moduleId);

        // 如果更换了父节点 重新确定原父节点的 leaf属性，以及所修改节点的orders属性
        if(null != moduleinNode
                && null != pid
                && !pid.equals(moduleinNode.getParentId())){
            int brothers = this.mapper.countPrantLeaf(moduleId) - 1;
            if(brothers < 1){
                moduleinNode.setIsleaf(true);
                this.updateModule(moduleinNode);
            }
            if (null == module.getOrders()){
                int buddy = this.mapper.countPrantLeaf(pid);
                module.setOrders(buddy);
            }

        }

        this.merge(module);
        // 保存完重新查询一遍列表数据
        return this.listModule(null);
    }

    /**
     * 根据ID更新
     * @param moduleEntity
     * @return
     */
    public void updateModule(ModuleEntity moduleEntity){
        this.mapper.updateById(moduleEntity);
    }


    /**
     * 根据ID获取一条模块信息
     * @param id 模块ID
     * @return 模块信息
     */
    public ModuleEntity getModule(String id) {
        return this.getById(id);
    }

    /**
     * 根据属性查询模块树列表
     * @param module 属性实体类
     * @return 模块树列表
     */
    public List<ModuleEntity> listModuleByAttr(ModuleEntity module) {
        return this.mapper.listModule(null, module);
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
        entityWrapper.eq("parent_id", module.getParentId());

        int records = this.mapper.selectCount(entityWrapper);
        return records > 0;
    }
}