package org.pkaq.sys.organization.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.pkaq.sys.organization.bo.OrganizationAoeBo;
import org.pkaq.sys.organization.bo.OrganizationQueryBo;
import org.pkaq.sys.organization.bo.OrganizationSortBo;
import org.pkaq.sys.organization.convert.OrganizationConvert;
import org.pkaq.sys.organization.entity.OrganizationEntity;
import org.pkaq.sys.organization.mapper.OrganizationMapper;
import org.pkaq.sys.organization.vo.OrganizationDetailVo;
import org.pkaq.sys.organization.vo.OrganizationListVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 组织信息Service
 *
 * @author S.PKAQ
 */
@Service
@RequiredArgsConstructor
public class OrganizationService extends StdService<OrganizationMapper, OrganizationEntity, OrganizationConvert> {
    private final OrganizationConvert organizationConvert;
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
    public void deleteOrg(Set<Long> ids) {
        // 检查是否存在子节点，存在子节点不允许删除
        LambdaQueryWrapper<OrganizationEntity> oew = new LambdaQueryWrapper<>();
        oew.in(OrganizationEntity::getPid, ids);

        List<OrganizationEntity> leafList = this.mapper.selectList(oew);

        if (CollUtil.isNotEmpty(leafList)) {
            List<Object> list = CollUtil.getFieldValues(leafList, "parentName");
            String name = CollUtil.join(list, ",");

            CommonCodes.CHILD_EXIST.newException(name);
        } else {
            this.mapper.deleteByIds(ids);
        }
    }

    /**
     * 新增/编辑一条组织信息
     *
     * @param bo 要 新增/编辑 得组织对象
     */
    public void editOrg(OrganizationAoeBo bo) {
//        var organization = this.organizationConvert.aoeBoToEntity(bo);
//
//        Long orgId = organization.getId();
//        // 获取上级节点
//        String pid = organization.getPid();
//        String root = "0";
//        if (!root.equals(pid) && StrUtil.isNotBlank(pid)) {
//            // 查询新父节点信息
//            OrganizationEntity parentOrg = this.get(pid);
//            // 设置当前节点信息
//            String parentPath = StrUtil.isNotBlank(organization.getId()) ? parentOrg.getPath() + "/" + organization.getId() : parentOrg.getPath();
//            organization.setPath(parentPath);
//
//        } else {
//            // 父节点为空, 根节点 设置为非叶子\
//            pid = root;
//            if (StrUtil.isNotBlank(organization.getId())) {
//                organization.setPath(organization.getId());
//            }
//            organization.setPid(pid);
//            organization.setIsleaf(false);
//        }
//
//        // 检查原父节点是否还存在子节点 不存在设置leaf为false
//        OrganizationEntity orginNode = this.mapper.getParentById(orgId);
//
//        // 如果更换了父节点 重新确定原父节点的 leaf属性，以及所修改节点的orders属性
//        if (null != orginNode && !pid.equals(orginNode.getPid())) {
//            int brothers = this.mapper.countPrantLeaf(orgId) - 1;
//            if (brothers < 1) {
//                orginNode.setIsleaf(true);
//                this.updateOrg(orginNode);
//            }
//        }
//        //如果是新增且orders属性为空则设置orders属性
//        OrganizationEntity oldOrgin = null;
//        if (CharSequenceUtil.isBlank(organization.getId())) {
//            organization.setSort(this.mapper.countPrantLeaf(pid));
//        } else {
//            oldOrgin = this.mapper.selectById(orgId);
//        }
//        this.merge(organization);
//
//        //新增
//        if (null == oldOrgin) {
//            //设置path路径 把path路径加上自己本身
//            //String path= StrUtil.isBlank(organization.getPath()) ? organization.getId() : organization.getPath() + "/" + organization.getId();
//            this.mapper.updateById(organization);
//        } else {
//            //刷新子节点相关数据
//            this.refreshChild(organization, oldOrgin);
//        }
//        // 保存完重新查询一遍列表数据
    }

    // 父节点信息有修改 刷新子节点相关数据
    public void refreshChild(OrganizationEntity organizationEntity, OrganizationEntity oldOrgin) {
//        // 刷新子节点名称
//        this.mapper.updateChildParentName(organizationEntity.getName(), organizationEntity.getId());
//        // TODO 刷新所有子节点的 path_name 和 path
//        this.mapper.updateChildPathInfo(organizationEntity, oldOrgin);
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
    public OrganizationDetailVo getOrg(long id) {
        return this.get(id);
    }

    /**
     * 根据属性查询组织树列表
     *
     * @return 组织树列表
     */
    public List<OrganizationListVo> list(OrganizationQueryBo queryBo) {
        var bo = this.organizationConvert.queryBoToEntity(queryBo);
        var vo = this.mapper.listOrg(bo);
        return this.organizationConvert.entityToListVo(vo);
    }

    /**
     * 交换两个orders值
     *
     * @param switchOrg 进行交换的两个实体
     */
    public void sortOrg(OrganizationSortBo[] switchOrg) {
        // 优化逻辑
//        for (OrganizationEntity org : switchOrg) {
//            this.mapper.updateById(org);
//        }
    }

    /**
     * 切换可用状态 - 级联操作
     * @param ids
     */
    public void switchStatus(SingleArray<String> ids) {
        //TODO 优化逻辑
        //this.mapper.switchStatus(organization);
    }
}
