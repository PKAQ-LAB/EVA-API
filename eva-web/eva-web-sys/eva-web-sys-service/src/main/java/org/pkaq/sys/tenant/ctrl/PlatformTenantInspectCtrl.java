package org.pkaq.sys.tenant.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.sys.tenant.service.PlatformTenantInspectService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台跨租户只读查看控制器。
 *
 * @author PKAQ
 */
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "eva", name = "mode", havingValue = "platform")
@RequestMapping("/sys/platform/tenants")
public class PlatformTenantInspectCtrl extends Ctrl {
    private final PlatformTenantInspectService service;

    @GetMapping("/options")
    @Operation(summary = "查询可查看的租户选项")
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询了平台租户选项")
    public Response<Object> options() {
        return Response.success(this.service.listTenantOptions());
    }

    @GetMapping("/{tenantId}/organizations")
    @Operation(summary = "跨租户查询组织树")
    @BizLog(operateType = BizLogCodes.QUERY,
            description = "平台查看了目标租户组织[{0}]", args = {"param:0"})
    public Response<Object> organizations(@PathVariable Long tenantId) {
        return Response.success(this.service.listOrganizations(tenantId));
    }

    @GetMapping("/{tenantId}/roles")
    @Operation(summary = "跨租户查询角色列表")
    @BizLog(operateType = BizLogCodes.QUERY,
            description = "平台查看了目标租户角色[{0}]", args = {"param:0"})
    public Response<Object> roles(@PathVariable Long tenantId) {
        return Response.success(this.service.listRoles(tenantId));
    }
}
