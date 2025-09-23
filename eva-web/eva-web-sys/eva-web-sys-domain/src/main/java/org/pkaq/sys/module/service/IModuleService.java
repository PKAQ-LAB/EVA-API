package org.pkaq.sys.module.service;

import org.pkaq.sys.module.bo.ModuleAoeBo;
import org.pkaq.sys.module.bo.ModuleQueryBo;
import org.pkaq.sys.module.bo.ModuleSortBo;
import org.pkaq.sys.module.vo.ModuleDetailVo;
import org.pkaq.sys.user.vo.UserResourceVo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author PKAQ
 */
public interface IModuleService {
    void deleteModule(Set<Long> ids);

    void editModule(ModuleAoeBo bo);

    ModuleDetailVo getModule(Long id);

    Collection<ModuleDetailVo> list(ModuleQueryBo queryBo);

    void sortModule(ModuleSortBo[] switchModule);

    boolean checkUnique(ModuleAoeBo module);

    List<UserResourceVo> fetchModuleByUid(Long uid);
}
