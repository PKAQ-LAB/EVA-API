package io.nerv.web.sys.organization.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.nerv.core.mvc.service.BaseService;
import io.nerv.core.mvc.util.Response;
import io.nerv.web.sys.organization.entity.OrganizationEntity;
import io.nerv.web.sys.organization.mapper.OrganizationMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织信息Service
 * @author S.PKAQ
 */
@Service
public class OrganizationService extends BaseService<OrganizationMapper, OrganizationEntity> {

    /**
     * 查询组织结构树
     * @return
     */
    public List<OrganizationEntity> listOrg(String condition){
        return this.mapper.listOrg(condition, null);
    }

    /**
     * 根据ID批量删除
     * @param ids
     * @return
     */
    public Response deleteOrg(ArrayList<String> ids){
        Response response = null;
        // 检查是否存在子节点，存在子节点不允许删除
        QueryWrapper<OrganizationEntity> oew = new QueryWrapper<>();
        oew.setEntity( new OrganizationEntity() );
        oew.in("parent_ID", ids);

        List<OrganizationEntity> leafList = this.mapper.selectList(oew);

        if (CollectionUtil.isNotEmpty(leafList)){
            List<Object> list = CollectionUtil.getFieldValues(leafList, "parentName");
            String name = CollectionUtil.join(list, ",");
            response = new Response().failure(501, StrUtil.format("[{}] 存在子节点，无法删除。",name), null);
         } else {
            this.mapper.deleteBatchIds(ids);
            response = new Response().success(this.mapper.listOrg(null, null));
        }

        return response;
    }

    /**
     * 新增/编辑一条组织信息
     * @param organization 要 新增/编辑 得组织对象
     * @return 重新查询组织列表
     */
    public List<OrganizationEntity> editOrg(OrganizationEntity organization){
        String orgId = organization.getId();
        // 获取上级节点
        String pid = organization.getParentId();
        String root = "0";
        if(!root.equals(pid) && StrUtil.isNotBlank(pid)){
            // 查询新父节点信息
            OrganizationEntity parentOrg = this.getOrg(pid);
            // 设置当前节点信息
            String parentPath = StrUtil.isNotBlank(parentOrg.getPath())?
                    parentOrg.getPath()+","+parentOrg.getId():
                    parentOrg.getId();
            organization.setPath(parentPath);
            String pathName = StrUtil.format("{}/{}", parentOrg.getName(), organization.getName());
            organization.setPathName(pathName);
            organization.setParentName(parentOrg.getName());

        } else {
            // 父节点为空, 根节点 设置为非叶子\
            pid = root;

            organization.setParentId(pid);
            organization.setIsleaf(false);
            organization.setPath(organization.getId());
            organization.setPathName(organization.getName());
        }

        // 检查原父节点是否还存在子节点 不存在设置leaf为false
        OrganizationEntity orginNode = this.mapper.getParentById(orgId);

        // 如果更换了父节点 重新确定原父节点的 leaf属性，以及所修改节点的orders属性
        if(null != orginNode && !pid.equals(orginNode.getParentId())){
            int brothers = this.mapper.countPrantLeaf(orgId) - 1;
            if(brothers < 1){
                orginNode.setIsleaf(true);
                this.updateOrg(orginNode);
            }
        }
        //如果是新增且orders属性为空则设置orders属性
        if (StrUtil.isBlank(organization.getId()) ) {
            organization.setOrders(this.mapper.countPrantLeaf(pid));
        }
        this.merge(organization);
        // 保存完重新查询一遍列表数据
        return this.listOrg(null);
    }

    // 父节点信息有修改 刷新子节点相关数据
    public void refreshChild(OrganizationEntity organizationEntity){
        // 刷新子节点名称
        this.mapper.updateChildParentName(organizationEntity.getName(), organizationEntity.getId());
        // TODO 刷新所有子节点的 path_name 和 path
        //this.mapper.updateChildPathInfo(module.getName(),module.getPath(),module.getId());
        //-- 所有子节点刷新
        //update sys_module set
        //        PATH_NAME=REPLACE(PARENT_NAME,name,'基础信息'),
        //        PATH=REPLACE(PATH,'oldPath','newPath')
        //where path like concat(path,'%') and path_id like '054d3ed0e60f4faaa29ceb1a440375f3%'

    }
    /**
     * 根据ID更新
     * @param organizationEntity
     * @return
     */
    public void updateOrg(OrganizationEntity organizationEntity){
        // 检查是否存在叶子节点，存在 返回叶子节点名称 终止删除
        this.mapper.updateById(organizationEntity);
    }

    /**
     * 新增
     * @param organizationEntity
     * @return
     */
    public void insertOrg(OrganizationEntity organizationEntity){
        this.mapper.insert(organizationEntity);
    }

    /**
     * 根据ID获取一条组织信息
     * @param id 组织ID
     * @return 组织信息
     */
    public OrganizationEntity getOrg(String id) {
        return this.getById(id);
    }

    /**
     * 根据属性查询组织树列表
     * @param organization 属性实体类
     * @return 组织树列表
     */
    public List<OrganizationEntity> listOrgByAttr(OrganizationEntity organization) {
        return  this.mapper.listOrg(null, organization);
    }

    /**
     *  交换两个orders值
     * @param switchOrg 进行交换的两个实体
     */
    public void sortOrg(OrganizationEntity[] switchOrg) {
        for (OrganizationEntity org : switchOrg) {
            this.mapper.updateById(org);
        }
    }

    /**
     * 切换可用状态 - 级联操作
     * @param organization
     */
    public void switchStatus(OrganizationEntity organization) {
        this.mapper.switchStatus(organization);
    }

    /**
     * 校验code是否唯一
     * @param organization
     * @return
     */
    public boolean checkUnique(OrganizationEntity organization) {
        QueryWrapper<OrganizationEntity> entityWrapper = new QueryWrapper<>();
        entityWrapper.eq("code", organization.getCode());
        int records = this.mapper.selectCount(entityWrapper);
        return records > 0;
    }
}
