package org.pkaq.web.organization.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import org.pkaq.core.util.Response;
import org.pkaq.web.organization.entity.OrganizationEntity;
import org.pkaq.web.organization.mapper.OrganizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrganizationService {
    @Autowired
    private OrganizationMapper organizationMapper;

    /**
     * 查询组织结构树
     * @return
     */
    public List<OrganizationEntity> listOrg(String condition){
        return this.organizationMapper.listOrg(condition, null);
    }

    /**
     * 根据ID批量删除
     * @param ids
     * @return
     */
    public Response deleteOrg(ArrayList<String> ids){
        Response response = null;
        // 检查是否存在子节点，存在子节点不允许删除
        EntityWrapper<OrganizationEntity> oew = new EntityWrapper<>();
        oew.setEntity( new OrganizationEntity() );
        oew.in("parentID", ids);

        List<OrganizationEntity> leafList = this.organizationMapper.selectList(oew);

        if (CollectionUtil.isNotEmpty(leafList)){
            List<Object> list = CollectionUtil.getFieldValues(leafList, "parentName");
            String name = CollectionUtil.join(list, ",");
            response = new Response().failure(501, StrUtil.format("[{}] 存在子节点，无法删除。",name), null);
         } else {
            this.organizationMapper.deleteBatchIds(ids);
            response = new Response().success(this.organizationMapper.selectList(new EntityWrapper<>()));
        }

        return response;
    }

    /**
     * 根据ID更新
     * @param organizationEntity
     * @return
     */
    public Integer updateOrg(OrganizationEntity organizationEntity){
        // 检查是否存在叶子节点，存在 返回叶子节点名称 终止删除
        return this.organizationMapper.updateById(organizationEntity);
    }

    /**
     * 根据条件更新
     * @param organizationEntity
     * @return
     */
    public Integer updateByCondition(OrganizationEntity organizationEntity){
        EntityWrapper<OrganizationEntity> condition = new EntityWrapper<>();
        condition.setEntity(organizationEntity);
        return this.organizationMapper.update(organizationEntity, condition);
    }

    /**
     * 新增
     * @param organizationEntity
     * @return
     */
    public Integer insertOrg(OrganizationEntity organizationEntity){
        return this.organizationMapper.insert(organizationEntity);
    }

    /**
     * 根据ID获取一条组织信息
     * @param id 组织ID
     * @return 组织信息
     */
    public OrganizationEntity getOrg(String id) {
        return  this.organizationMapper.selectById(id);
    }

    /**
     * 根据属性查询组织树列表
     * @param organization 属性实体类
     * @return 组织树列表
     */
    public List<OrganizationEntity> listOrgByAttr(OrganizationEntity organization) {
        return  this.organizationMapper.listOrg(null, organization);
    }
}
