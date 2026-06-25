package org.pkaq.sys.tenant.service;

import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.vo.PageVo;
import org.pkaq.sys.tenant.bo.TenantAoeBo;
import org.pkaq.sys.tenant.bo.TenantCheckBo;
import org.pkaq.sys.tenant.bo.TenantQueryBo;
import org.pkaq.sys.tenant.vo.TenantDetailVo;
import org.pkaq.sys.tenant.vo.TenantListVo;

import java.util.Set;

/**
 * @author PKAQ
 */
public interface ITenantService {

    /**
     * 批量切换冻结状态
     * 内部按当前 frozen 翻转：FROZEN ↔ UN_FROZEN，READ_ONLY 保护跳过；
     * 翻转为 FROZEN 时同步冻结该租户下所有用户并踢下线
     */
    void switchFrozen(SingleArray<Long> ids);

    /**
     * 批量删除租户：同步软删该租户下所有用户并踢下线
     */
    void delete(Set<Long> ids);

    /**
     * 新增 / 编辑租户。新增时一并创建管理员账号，user.id = tenant.adminId
     */
    void edit(TenantAoeBo editBo);

    /**
     * 获取详情（含管理员账号信息）
     */
    TenantDetailVo get(Long id);

    /**
     * 分页查询
     */
    PageVo<TenantListVo> listPage(TenantQueryBo queryBo);

    /**
     * 校验 code / name 唯一性
     */
    boolean checkUnique(TenantCheckBo checkBo);
}
