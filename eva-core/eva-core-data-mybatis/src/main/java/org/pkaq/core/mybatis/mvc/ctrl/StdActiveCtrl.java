package org.pkaq.core.mybatis.mvc.ctrl;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.codes.ResponseCodes;
import org.pkaq.core.mvc.bo.SingleArrayBo;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.mybatis.mvc.service.StdActiveService;
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
public abstract class StdActiveCtrl<T extends StdActiveService, E extends Model> extends Ctrl {
    @Autowired
    protected T service;

    @PostMapping("del")
    @Operation(summary = "删除记录", description = "根据ID删除/批量删除记录")
    public Response del(@Parameter(name = "ids", description = "[记录ID]")
                        @RequestBody SingleArrayBo<String> ids) {

        CommonCodes.NULL_ID.assertNotNull(ids);
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());

        this.service.delete(ids.getParam());
        return success(this.service.listPage(null, 1), ResponseCodes.DELETE_SUCCESS);
    }

    @PostMapping("edit")
    @Operation(summary = "新增记录", description = "新增/编辑记录")
    public Response save(@Parameter(name = "formdata", description = "模型对象")
                         @RequestBody E entity) {
        this.service.merge(entity);
        return success(entity, ResponseCodes.SAVE_SUCCESS);
    }

    @GetMapping("list")
    @Operation(summary = "分页查询", description = "列表查询")
    public Response list(@Parameter(name = "condition", description = "模型对象")
                         E entity, Integer pageNo, Integer pageSize) {
        return this.success(this.service.listPage(entity, pageNo, pageSize));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID查询", description = "根据ID获得记录信息")
    public Response get(@Parameter(name = "id", description = "记录ID")
                        @PathVariable("id") String id) {
        return this.success(this.service.getById(id));
    }
}
