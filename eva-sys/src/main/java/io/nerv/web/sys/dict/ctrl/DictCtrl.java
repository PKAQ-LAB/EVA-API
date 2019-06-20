package io.nerv.web.sys.dict.ctrl;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.enums.HttpCodeEnum;
import io.nerv.core.mvc.ctrl.BaseCtrl;
import io.nerv.core.mvc.util.Response;
import io.nerv.web.sys.dict.entity.DictEntity;
import io.nerv.web.sys.dict.helper.DictHelperProvider;
import io.nerv.web.sys.dict.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.nerv.core.exception.ParamException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 字典管理控制器
 * @author: S.PKAQ
 * @Datetime: 2018/3/15 0:11
 */
@Api( tags = "字典管理")
@RestController
@RequestMapping("/dict")
public class DictCtrl extends BaseCtrl<DictService> {
    @Autowired
    private DictHelperProvider dictHelper;

    @GetMapping({"/query/{code}"})
    @ApiOperation(value = "根据 code 从缓存中获取字典项",response = Response.class)
    public Response query(@ApiParam(name = "code", value = "字典分类ID")
                          @PathVariable(name = "code", required = false) String code){
        return this.success(dictHelper.get(code));
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取字典分类列表",response = Response.class)
    public Response listDict(){
        return new Response().success(this.service.listDict());
    }

    @GetMapping({"/get/{id}", "/get/type/{code}"})
    @ApiOperation(value = "根据ID获取字典",response = Response.class)
    public Response getDict(@ApiParam(name = "id", value = "字典分类ID")
                            @PathVariable(name = "id", required = false) String id,
                            @ApiParam(name = "code", value = "类型编码")
                            @PathVariable(value = "code", required = false) String code){
        // 参数校验
        if (StrUtil.isBlank(id) && StrUtil.isBlank(code)){
            return failure(HttpCodeEnum.REQEUST_FAILURE.getIndex());
        }
        DictEntity dictEntity = new DictEntity();
        dictEntity.setId(id);
        dictEntity.setCode(code);

        return success(this.service.getDict(dictEntity));
    }

    @PostMapping("/checkUnique")
    @ApiOperation(value = "校验code",response = Response.class)
    public Response checkUnique(@ApiParam(name ="dictEntity", value = "要进行校验的参数")
                                @RequestBody DictEntity dictEntity){
        boolean exist = this.service.checkUnique(dictEntity);
        return exist? failure(): success();
    }

    @GetMapping("/del/{id}")
    @ApiOperation(value = "根据ID删除",response = Response.class)
    @PreAuthorize("hasRole('ADMIN')")
    public Response delDict(@ApiParam(name = "id", value = "[字典ID]")
                            @PathVariable("id") String id){
        // 参数非空校验
        if (StrUtil.isBlank(id)){
            throw new ParamException(locale("param_id_notnull"));
        }
        this.service.delDict(id);
        return success(this.service.listDict());
    }
    @PostMapping("/edit")
    @ApiOperation(value = "新增/编辑字典分类",response = Response.class)
    public Response editDict(@ApiParam(name ="organization", value = "字典信息")
                             @RequestBody @Valid DictEntity dictEntity){
        this.service.edit(dictEntity);

        return success(this.service.listDict());
    }
}
