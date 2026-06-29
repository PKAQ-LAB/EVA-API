package org.pkaq.core.auth.openapi.log.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.openapi.log.bo.OpenApiCallLogQueryBo;
import org.pkaq.core.auth.openapi.log.service.OpenApiCallLogService;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OpenAPI调用日志控制器。
 *
 * @author PKAQ
 */
@RestController
@RequestMapping("/monitor/log/openapi")
@Tag(name = "接口调用日志")
@RequiredArgsConstructor
public class OpenApiCallLogCtrl extends Ctrl {
    private final OpenApiCallLogService openApiCallLogService;

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获取接口调用日志详情")
    public Response<Object> get(@Parameter(name = "id", description = "接口调用日志ID")
                                @PathVariable("id") Long id) {
        return success(this.openApiCallLogService.get(id));
    }

    @GetMapping("/list")
    @Operation(summary = "获取接口调用日志列表")
    public Response<Object> list(OpenApiCallLogQueryBo queryBo) {
        return success(this.openApiCallLogService.list(queryBo));
    }
}
