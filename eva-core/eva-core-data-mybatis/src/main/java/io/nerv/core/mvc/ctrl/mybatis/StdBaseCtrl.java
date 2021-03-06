package io.nerv.core.mvc.ctrl.mybatis;

import cn.hutool.core.collection.CollectionUtil;
import io.nerv.core.annotation.NoRepeatSubmit;
import io.nerv.core.enums.BizCode;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.enums.ResponseEnumm;
import io.nerv.core.exception.ParamException;
import io.nerv.core.mvc.ctrl.Ctrl;
import io.nerv.core.mvc.entity.mybatis.StdBaseEntity;
import io.nerv.core.mvc.service.mybatis.StdBaseService;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.mvc.vo.SingleArray;
import io.nerv.core.util.I18NHelper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller 基类
 * @author S.PKAQ
 */
@Getter
public abstract class StdBaseCtrl<T extends StdBaseService, E extends StdBaseEntity> extends Ctrl {
    @Autowired
    protected T service;

    @PostMapping("/del")
    @ApiOperation(value = "根据ID删除/批量删除记录",response = Response.class)
    @NoRepeatSubmit
    public Response del(@ApiParam(name = "ids", value = "[记录ID]")
                        @RequestBody SingleArray<String> ids){

        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }
        this.service.delete(ids.getParam());
        return success(null, ResponseEnumm.DELETE_SUCCESS.getName());
    }

    @PostMapping("/edit")
    @ApiOperation(value = "新增/编辑记录",response = Response.class)
    @NoRepeatSubmit
    public Response save(@ApiParam(name ="formdata", value = "模型对象")
                         @RequestBody E entity){
        this.service.merge(entity);
        return success(entity.getId(), ResponseEnumm.SAVE_SUCCESS.getName());
    }

    @GetMapping("/list")
    @ApiOperation(value = "列表查询",response = Response.class)
    @NoRepeatSubmit
    public Response list(@ApiParam(name ="condition", value = "模型对象")
                                 E entity, Integer pageNo, Integer pageSize){
        return this.success(this.service.listPage(entity, pageNo, pageSize));
    }

    @GetMapping("/listAll")
    @ApiOperation(value = "列表查询 无分页",response = Response.class)
    @NoRepeatSubmit
    public Response listAll(@ApiParam(name ="condition", value = "模型对象")
                                 E entity){
        return this.success(this.service.list(entity));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据ID获得记录信息", response = Response.class)
    @NoRepeatSubmit
    public Response get(@ApiParam(name = "id", value = "记录ID")
                            @PathVariable("id") String id){
        return this.success(this.service.getById(id));
    }
}
