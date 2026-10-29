package org.pkaq.sys.module.mapper;

import lombok.Data;
import org.pkaq.sys.module.vo.ModuleMenuResourceVo;
import org.pkaq.sys.module.vo.ModuleMenuVo;

/**
 * 已授权菜单的最小扁平投影，不承载业务实体或审计字段。
 * @author PKAQ
 * @date 2026-10-28
 */
@Data
public class AuthorizedMenuRow {
    private ModuleMenuVo module;
    private ModuleMenuResourceVo resource;
}
