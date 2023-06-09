package tech.yunyue.param.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tech.yunyue.core.mvc.ctrl.Ctrl;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.param.bo.SystemParameterEditBo;
import tech.yunyue.param.service.SystemParameterService;

import java.util.List;

/**
 * @author : dmz
 */
@Tag(name  = "系统参数设置对象功能接口")
@RestController
@RequestMapping("/sys/param")
@RequiredArgsConstructor
public class SystemParameterCtrl extends Ctrl {
    /**
     * 系统参数设置Service
     */
    private final SystemParameterService systemParameterService;

    @GetMapping("/get")
    @Operation(summary = "获取系统参数")
    public Response get() {
        return this.success(this.systemParameterService.get());
    }

    @PostMapping("/edit")
    @Operation(summary = "编辑系统参数设置")
    public Response edit(@Parameter(name = "edit", description = "编辑")
                         @RequestBody @Valid List<SystemParameterEditBo> bo) {
        systemParameterService.edit(bo);
        return this.success();
    }


}
