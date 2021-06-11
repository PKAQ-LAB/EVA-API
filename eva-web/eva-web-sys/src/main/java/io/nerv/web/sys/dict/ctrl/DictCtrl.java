package io.nerv.web.sys.dict.ctrl;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.exception.ParamException;
import io.nerv.core.mvc.ctrl.Ctrl;
import io.nerv.core.mvc.vo.Response;
import io.nerv.web.sys.dict.cache.DictCacheHelper;
import io.nerv.web.sys.dict.entity.DictEntity;
import io.nerv.web.sys.dict.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 字典管理控制器
 * @author: S.PKAQ
 */
@Api("字典管理")
@RestController
@RequestMapping("/sys/dictionary")
@RequiredArgsConstructor
public class DictCtrl extends Ctrl {
    private final DictService service;

    private final DictCacheHelper dictCacheHelper;

    @GetMapping({"/query/{code}"})
    @ApiOperation(value = "根据 code 从缓存中获取字典项",response = Response.class)
    public Response query(@ApiParam(name = "code", value = "字典分类ID")
                          @PathVariable(name = "code", required = false) String code){
        return this.success(dictCacheHelper.get(code));
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取字典分类列表",response = Response.class)
    public Response listDict(){
        return this.success(this.service.listDict());
    }

    @GetMapping({"/get/{id}", "/get/type/{code}"})
    @ApiOperation(value = "根据ID获取字典",response = Response.class)
    public Response getDict(@ApiParam(name = "id", value = "字典分类ID")
                            @PathVariable(name = "id", required = false) String id,
                            @ApiParam(name = "code", value = "类型编码")
                            @PathVariable(value = "code", required = false) String code){
        // 参数校验
        if (StrUtil.isBlank(id) && StrUtil.isBlank(code)){
            throw new ParamException();
        }
        DictEntity dictEntity = new DictEntity();
        dictEntity.setId(id);
        dictEntity.setCode(code);

        return this.success(this.service.getDict(dictEntity));
    }

    @PostMapping("/checkUnique")
    @ApiOperation(value = "校验code",response = Response.class)
    public Response checkUnique(@ApiParam(name ="dictEntity", value = "要进行校验的参数")
                                @RequestBody DictEntity dictEntity){
        boolean exist = (null != dictEntity && StrUtil.isNotBlank(dictEntity.getCode()))? this.service.checkUnique(dictEntity) : false;
        return exist? this.failure(): this.success();
    }

    @GetMapping("/del/{id}")
    @ApiOperation(value = "根据ID删除",response = Response.class)
    public Response delDict(@ApiParam(name = "id", value = "[字典ID]")
                            @PathVariable("id") String id){
        // 参数非空校验
        if (StrUtil.isBlank(id)){
            throw new ParamException(locale("param_id_notnull"));
        }
        this.service.delDict(id);
        return success();
    }
    @PostMapping("/edit")
    @ApiOperation(value = "新增/编辑字典分类",response = Response.class)
    public Response editDict(@ApiParam(name ="organization", value = "字典信息")
                             @RequestBody @Valid DictEntity dictEntity){
        this.service.edit(dictEntity);

        return this.success();
    }
}
