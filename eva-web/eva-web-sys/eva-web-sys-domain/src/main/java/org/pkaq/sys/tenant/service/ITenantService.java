package org.pkaq.sys.tenant.service;

import org.pkaq.core.mvc.vo.SingleArray;
import org.pkaq.sys.tenant.bo.TenantAoeBo;
import org.pkaq.sys.tenant.bo.TenantCheckBo;
import org.pkaq.sys.tenant.vo.TenantDetailVo;

import java.util.List;

/**
 * @author PKAQ
 */
public interface ITenantService {
    void switchFrozen(SingleArray<Long> ids);

    void delete(List<Long> ids);

    void edit(TenantAoeBo editBo);

    TenantDetailVo get(String id);

    boolean checkUnique(TenantCheckBo checkBo);
}
