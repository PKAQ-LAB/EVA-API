package org.pkaq.core.auth.spi;

import org.pkaq.core.auth.spi.model.RoleSnapshot;
import java.util.List;

/**
 * 当前账号可信角色与数据范围的查询端口。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public interface IPermissionSnapshotQuery {
    /**
     * 在当前可信租户上下文内读取权限快照。
     * @param userId 用户编号
     * @return 有效角色列表
     */
    List<RoleSnapshot> findRoles(Long userId);
}
