package tech.yunyue.sys.organization.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import tech.yunyue.sys.organization.entity.OrganizationEntity;
import tech.yunyue.sys.organization.mapper.OrganizationMapper;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.mybatis.mvc.service.mybatis.StdService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织信息Service
 *
 * @author S.PKAQ
 */
@Service
public class OrganizationService extends StdService<OrganizationMapper, OrganizationEntity> {

    /**
     * 查询组织结构树
     *
     * @return
     */
    public List<OrganizationEntity> listOrg(OrganizationEntity organizationEntity) {
        return this.mapper.listOrg(organizationEntity);
    }

    /**
     * 根据ID批量删除
     *
     * @param ids
     * @return
     */
    public Response deleteOrg(ArrayList<String> ids) {
        Response response = null;
        // 检查是否存在子节点，存在子节点不允许删除
        QueryWrapper<OrganizationEntity> oew = new QueryWrapper<>();
        oew.in("parent_ID", ids);

        List<OrganizationEntity> leafList = this.mapper.selectList(oew);

        if (CollectionUtil.isNotEmpty(leafList)) {
            List<Object> list = CollectionUtil.getFieldValues(leafList, "parentName");
            String name = CollectionUtil.join(list, ",");

            BizCodeEnum.CHILD_EXIST.newException(name);
        } else {
            this.mapper.deleteBatchIds(ids);
            response = new Response().success();
        }

        return response;
    }

    /**
     * 新增/编辑一条组织信息
     *
     * @param organization 要 新增/编辑 得组织对象
     */
    public void editOrg(OrganizationEntity organization) {
        String orgId = organization.getId();
        boolean isNew = StrUtil.isBlank(orgId);

        // 获取上级节点
        String pid = organization.getParentId();
        String root = "0";
        if (!root.equals(pid) && StrUtil.isNotBlank(pid)) {
            // 查询新父节点信息
            OrganizationEntity parentOrg = this.getOrg(pid);
            // 设置当前节点信息  当前若是新增 则只要把自己的id加在path后即可
            String parentPath = parentOrg.getPath() + "/" + (StrUtil.isNotBlank(orgId) ? orgId : "");
            organization.setPath(parentPath);
            String pathName = parentOrg.getPathName() + "/" + organization.getName();
            organization.setPathName(pathName);
            organization.setParentName(parentOrg.getName());
        } else {
            // 父节点为空, 根节点 设置为非叶子
            pid = root;
            organization.setPath(orgId);
            organization.setParentId(pid);
            organization.setIsleaf(false);
            organization.setPathName(organization.getName());
        }

        // 检查原父节点是否还存在子节点 不存在设置leaf为false
        OrganizationEntity orginNode = this.mapper.getParentById(orgId);
        boolean isChangePa = false;
        // 如果更换了父节点 重新确定原父节点的 leaf属性，以及所修改节点的orders属性
        if (null != orginNode && !pid.equals(orginNode.getId())) {
            int brothers = this.mapper.countPrantLeaf(orginNode.getId()) - 1;
            if (brothers < 1) {
                orginNode.setIsleaf(true);
                this.updateOrg(orginNode);
            }
            isChangePa = true;
        }
        //新增或者更换了父节点则设置orders属性
        if (isNew || isChangePa) {
            organization.setOrders(this.mapper.countPrantLeaf(pid));
        }
        OrganizationEntity oldOrgin = isNew ? null : this.mapper.selectById(orgId);
        this.merge(organization);

        if (isNew) {
            //新增 把path路径加上自己id
            organization.setPath(organization.getPath() + organization.getId());
            this.mapper.updateById(organization);
        } else {
            //刷新子节点相关数据
            this.refreshChild(organization, oldOrgin);
        }
    }

    // 父节点信息有修改 刷新子节点相关数据
    public void refreshChild(OrganizationEntity organizationEntity, OrganizationEntity oldOrgin) {
        // 刷新所有子节点的path_name、path 以及直接子节点的parent_name
        this.mapper.updateChildPathInfo(organizationEntity, oldOrgin);
    }

    /**
     * 根据ID更新
     *
     * @param organizationEntity
     */
    public void updateOrg(OrganizationEntity organizationEntity) {
        // 检查是否存在叶子节点，存在 返回叶子节点名称 终止删除
        this.mapper.updateById(organizationEntity);
    }

    /**
     * 根据ID获取一条组织信息
     *
     * @param id 组织ID
     * @return 组织信息
     */
    public OrganizationEntity getOrg(String id) {
        return this.getById(id);
    }

    /**
     * 根据属性查询组织树列表
     *
     * @param organization 属性实体类
     * @return 组织树列表
     */
    public List<OrganizationEntity> list(OrganizationEntity organization) {
        return this.mapper.listOrg(organization);
    }

    /**
     * 组织排序
     *
     * @param switchOrg 进行交换的两个实体
     */
    public void sortOrg(OrganizationEntity[] switchOrg) {
        int i=0;
        for (OrganizationEntity org : switchOrg) {
            OrganizationEntity update = new OrganizationEntity();
            update.setId(org.getId());
            update.setOrders(i++);
            this.mapper.updateById(update);
        }
    }

    /**
     * 切换可用状态 - 级联操作
     *
     * @param organization
     */
    public void switchStatus(OrganizationEntity organization) {
        this.mapper.switchStatus(organization);
    }

    /**
     * 校验code是否唯一
     *
     * @param organization
     * @return
     */
    public boolean checkUnique(OrganizationEntity organization) {
        QueryWrapper<OrganizationEntity> entityWrapper = new QueryWrapper<>();
        entityWrapper.eq("code", organization.getCode());
        long records = this.mapper.selectCount(entityWrapper);
        return records > 0;
    }
}
