package org.pkaq.core.mybatis.mvc.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.pkaq.core.annotation.NoRepeatSubmit;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.codes.ResponseCodes;
import org.pkaq.core.mvc.bo.PageBo;
import org.pkaq.core.mvc.bo.SingleArrayBo;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller 基类
 *
 * @author S.PKAQ
 */
@Getter
public abstract class StdCtrl<T extends StdService, E extends StdEntity> extends Ctrl {
    @Resource
    protected T service;

    @PostMapping("/del")
    @Operation(summary = "删除记录", description = "根据ID删除/批量删除记录")
    @NoRepeatSubmit
    public Response<Object> del(@Parameter(name = "ids", description = "[记录ID]")
                        @RequestBody SingleArrayBo<String> ids) {

        CommonCodes.NULL_ID.assertNotNull(ids);
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());

        this.service.delete(ids.getParam());
        return success(null, ResponseCodes.DELETE_SUCCESS);
    }

    @PostMapping("/edit")
    @Operation(summary = "新增记录", description = "新增/编辑记录")
    @NoRepeatSubmit
    public Response<Object> save(@Parameter(name = "formdata", description = "模型对象")
                         @RequestBody E entity) {
        this.service.merge(entity);
        return success(entity, ResponseCodes.SAVE_SUCCESS);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询", description = "列表查询")
    @NoRepeatSubmit
    public Response<Object> list(@Parameter(name = "condition", description = "模型对象") E entity,
                         @Parameter(name = "page", description = "分页查询参数") PageBo page) {
        return this.success(this.service.listPage(page, entity));
    }

    @GetMapping("/listAll")
    @Operation(summary = "查询全部", description = "列表查询 无分页")
    @NoRepeatSubmit
    public Response<Object> listAll(@Parameter(name = "condition", description = "模型对象")
                            E entity) {
        return this.success(this.service.list(entity));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID查询", description = "根据ID获得记录信息")
    @NoRepeatSubmit
    public Response<Object> get(@Parameter(name = "id", description = "记录ID")
                        @PathVariable("id") String id) {
        return this.success(this.service.get(id));
    }
}
