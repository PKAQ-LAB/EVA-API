package org.pkaq.sys.tenant.service;

import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.sys.tenant.bo.TenantAoeBo;
import org.pkaq.sys.tenant.bo.TenantCheckBo;
import org.pkaq.sys.tenant.vo.TenantDetailVo;

import java.util.Set;

/**
 * @author PKAQ
 */
public interface ITenantService {
    void switchFrozen(SingleArray<Long> ids, Integer frozen);

    void delete(Set<Long> ids);

    void edit(TenantAoeBo editBo);

    TenantDetailVo get(String id);

    boolean checkUnique(TenantCheckBo checkBo);
}
