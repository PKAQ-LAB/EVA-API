package org.pkaq.core.auth.spi.model;

import lombok.Data;

/**
 * 仅权限应用使用的可选管理资料，不携带凭据。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Data
public class AccountProfileSnapshot {
    private Long accountId;
    private String code;
    private String name;
    private Long deptId;
}
