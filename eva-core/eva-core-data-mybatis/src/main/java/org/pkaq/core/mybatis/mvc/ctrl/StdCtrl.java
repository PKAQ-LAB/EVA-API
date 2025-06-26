package org.pkaq.core.mybatis.mvc.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import org.pkaq.core.annotation.NoRepeatSubmit;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mvc.bo.IdCodeBo;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.mybatis.mvc.service.StdService;
import org.springframework.beans.factory.annotation.Autowired;
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
public abstract class StdCtrl<T extends StdService> extends Ctrl {
    @Autowired
    protected T service;

    @PostMapping("/checkUnique")
    @Operation(summary = "校验code唯一性")
    public Response<Object> checkUnique(@Parameter(name = "idCodeBo", description = "要进行校验的参数")
                                        @RequestBody IdCodeBo idCodeBo) {
        var exist = this.service.isUnique(idCodeBo);
        return exist ? failure() : success();
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID查询", description = "根据ID获得记录信息")
    public Response<Object> get(@Parameter(name = "id", description = "记录ID")
                                @PathVariable("id") long id) {
        return this.success(this.service.get(id));
    }

    @PostMapping("/del")
    @Operation(summary = "删除记录", description = "根据ID删除/批量删除记录")
    public Response<Object> del(@Parameter(name = "ids", description = "[记录ID]")
                                @RequestBody SingleArray<Long> ids) {

        CommonCodes.NULL_ID.assertNotNull(ids.getParam());

        this.service.delete(ids.getParam());
        return success();
    }

    @PostMapping("/switch")
    @Operation(summary = "锁定/解锁")
    public Response<Object> change(@Parameter(name = "params", description = "[id]")
                                   @RequestBody SingleArray<Long> ids) {
        this.service.switchFrozen(ids);
        return success();
    }
}
