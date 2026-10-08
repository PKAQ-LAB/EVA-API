package org.pkaq.core.auth.spi.model;

import lombok.Data;

/**
 * 权限决策使用的角色快照，不携带持久化行为。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Data
public class RoleSnapshot {
    private Long id;
    private String name;
    private String code;
    private String dataScope;
    private String dataOrgIds;
}
