package tech.yunyue.sys.organization.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.enums.OrgTypeEnum;
import tech.yunyue.core.log.annotation.BizLog;
import tech.yunyue.core.log.base.BizLogEnum;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import tech.yunyue.core.util.json.JsonUtil;
import tech.yunyue.sys.param.entity.SystemParameterEntity;
import tech.yunyue.sys.param.mapper.SystemParameterMapper;
import tech.yunyue.sys.dict.cache.DictCacheHelper;
import tech.yunyue.sys.dict.service.DictService;
import tech.yunyue.sys.organization.entity.OrganizationEntity;
import tech.yunyue.sys.organization.mapper.OrganizationMapper;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.mybatis.mvc.service.mybatis.StdService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 组织信息Service
 *
 * @author S.PKAQ
 */
@Service
@Schema(description = "组织管理")
public class OrganizationService extends StdService<OrganizationMapper, OrganizationEntity> {
    @Autowired
    EvaConfig evaConfig;
    @Autowired
    DictCacheHelper dictCacheHelper;

    @Autowired
    DictService dictService;

    @Autowired
    SystemParameterMapper systemParameterMapper;
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
    @BizLog(operateType= BizLogEnum.DELETE,description = "删除组织[{0}]",args = {"param:0"})
    @Transactional
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
    @BizLog(operateType= BizLogEnum.CREATE_UPDATE,description = "保存组织[{0}]",args = {"param:0.id"})
    @Transactional
    public void editOrg(OrganizationEntity organization) {
        String orgId = organization.getId();
        boolean isNew = StrUtil.isBlank(orgId);

        // 获取上级节点
        String pid = organization.getParentId();
        String root = "0";
        //集团/公司用户的组织模块数据为空  新建的公司/部门需要手动加上父节点（集团/公司）
        if(isNew && StrUtil.isBlank(pid)){
            OrgTypeEnum orgTypeEnum = OrgTypeEnum.getByCode(organization.getType());
            switch (orgTypeEnum) {
                // 集团-->报错 非平台不能创建集团
                //公司的pid只能是集团id
                case COMPANY ->  pid = ThreadUserHelper.getTenantId();
                //公司用户创建部门，pid为公司id  集团用户创建部门，pid为集团id
                case DEPARTMENT -> pid = ThreadUserHelper.getOrgTenantId();
            }
            organization.setParentId(pid);
        }
        OrganizationEntity parentOrg = null;
        if (!root.equals(pid) && StrUtil.isNotBlank(pid)) {
            // 查询新父节点信息
            parentOrg = this.getOrg(pid);
            //子节点的类型不能大于父节点
            if (OrgTypeEnum.isLeapFrogging(parentOrg.getType(),organization.getType())) {
                String code= CommonConstant.ORGANIZATION_TYPE_CODE;
                BizCodeEnum.ORG_TYPE_INVALID.newException(dictCacheHelper.get(code,parentOrg.getType()),dictCacheHelper.get(code,organization.getType()));
            }
            // 设置当前节点信息  当前若是新增 则只要把自己的id加在path后即可
            String parentPath = parentOrg.getPath() + "/" + (isNew ? "" : orgId);
            organization.setPath(parentPath);
            String pathName = parentOrg.getPathName() + "/" + organization.getName();
            organization.setPathName(pathName);
            organization.setParentName(parentOrg.getName());
        } else {
            // 平台才能创建根节点
            BizCodeEnum.PERMISSION_EXPIRED.newException();
            // 父节点为空, 根节点 设置为非叶子
            pid = root;
            organization.setPath(isNew ? "" : orgId);
            organization.setParentId(pid);
            organization.setIsleaf(false);
            organization.setPathName(organization.getName());
        }

        // 检查原父节点是否还存在子节点 不存在设置leaf为false 以及判断新旧组织的租户信息是否一致
        OrganizationEntity oldOrgin = isNew ? null : this.mapper.selectById(orgId);
        boolean isChangePa = !isNew && !Objects.equals(oldOrgin.getParentId(), pid);
        if (isChangePa) {
            // 修改父节点后 重新确定原父节点的 leaf属性
            OrganizationEntity orginNode = this.mapper.getParentById(orgId);
            int brothers = this.mapper.countPrantLeaf(orginNode.getId()) - 1;
            if (brothers < 1) {
                orginNode.setIsleaf(true);
                this.updateOrg(orginNode);
            }
        }
        //新增或者更换了父节点则设置orders属性
        if (isNew || isChangePa) {
            organization.setOrders(this.mapper.countPrantLeaf(pid));
            if(pid != root) {
                organization.setTenantId(parentOrg.getTenantId());
                organization.setCompanyTenantId(parentOrg.getCompanyTenantId());
            }
            // 修改父节点后 判断新旧组织的租户信息是否一致
            if(isChangePa && (!Objects.equals(oldOrgin.getTenantId(), organization.getTenantId())
                                || !Objects.equals(oldOrgin.getCompanyTenantId(), organization.getCompanyTenantId()))){
                BizCodeEnum.NO_CHANGE_ORG.newException();
            }
        }

        this.merge(organization);

        if (isNew) {
            orgId = organization.getId();
            //新增 把path路径加上自己id
            organization.setPath(organization.getPath() + orgId);
            //公司和集团的租户id是自己
            switch (OrgTypeEnum.getByCode(organization.getType())) {
                case GROUP -> organization.setTenantId(orgId);
                case COMPANY ->  organization.setCompanyTenantId(orgId);
            }
            this.mapper.updateById(organization);
            //初始化集团数据
            initGroupData(organization);
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
    @BizLog(operateType= BizLogEnum.QUERY,description = "根据id查询组织")
    public OrganizationEntity getOrg(String id) {
        return this.getById(id);
    }

    /**
     * 根据属性查询组织树列表
     *
     * @param organization 属性实体类
     * @return 组织树列表
     */
    @BizLog(operateType= BizLogEnum.QUERY,description = "查询组织树")
    public List<OrganizationEntity> list(OrganizationEntity organization) {
        //租户模式且没有设置查询条件时  公司用户Parent为公司  集团用户Parent为集团
        if(StrUtil.isBlank(organization.getName()) && StrUtil.isBlank(organization.getParentId())){
            organization.setParentId(ThreadUserHelper.getOrgTenantId());
        }
        return this.mapper.listOrg(organization);
    }

    /**
     * 组织排序
     *
     * @param switchOrg 进行交换的两个实体
     */
    @BizLog(operateType= BizLogEnum.UPDATE,description = "调整组织顺序")
    @Transactional
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
    @BizLog(operateType= BizLogEnum.UPDATE,description = "切换组织可用状态")
    @Transactional
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
        if (StrUtil.isNotBlank(organization.getId())) {
            entityWrapper.ne("id", organization.getId());
        }
        long records = this.mapper.selectCount(entityWrapper);
        return records > 0;
    }

    /**
     * 创建集团后初始化集团数据
     */
    private void initGroupData(OrganizationEntity organization){
        if(!Objects.equals(organization.getType(),OrgTypeEnum.GROUP.getCode())){
            return;
        }
        String orgId = organization.getId();
        //初始化默认参数
        SystemParameterEntity paramEntity = new SystemParameterEntity();
        paramEntity.setCode(CommonConstant.BIZ_DICT_PARAMETER_CODE);
        paramEntity.setTenantId(orgId);
        // todo 拿到字典  之后再确定展示格式
        paramEntity.setCodeVal(JsonUtil.toJson(dictService.selectDict(CommonConstant.BIZ_DICT_CODE)));
        systemParameterMapper.insert(paramEntity);

        //初始化企业信息


    }
}
