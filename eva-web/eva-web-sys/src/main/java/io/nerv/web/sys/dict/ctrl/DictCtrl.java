package io.nerv.web.sys.dict.ctrl;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.BizException;
import io.nerv.core.mvc.ctrl.Ctrl;
import io.nerv.core.mvc.vo.Response;
import io.nerv.web.sys.dict.cache.DictCacheHelper;
import io.nerv.web.sys.dict.entity.DictEntity;
import io.nerv.web.sys.dict.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 字典管理控制器
 * @author: S.PKAQ
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/sys/dictionary")
@RequiredArgsConstructor
public class DictCtrl extends Ctrl {
    private final DictService service;

    private final DictCacheHelper dictCacheHelper;

    @GetMapping({"/query/{code}"})
    @Operation(summary = "根据 code 从缓存中获取字典项")
    public Response query(@Parameter(name = "code", description = "字典分类ID")
                          @PathVariable(name = "code", required = false) String code){
        return this.success(dictCacheHelper.get(code));
    }

    @GetMapping("/list")
    @Operation(summary = "获取字典分类列表")
    public Response listDict(){
        return this.success(this.service.listDict());
    }

    @GetMapping({"/get/{id}", "/get/type/{code}"})
    @Operation(summary = "根据ID获取字典")
    public Response getDict(@Parameter(name = "id", description = "字典分类ID")
                            @PathVariable(name = "id", required = false) String id,
                            @Parameter(name = "code", description = "类型编码")
                            @PathVariable(value = "code", required = false) String code){
        // 参数校验
        if (StrUtil.isBlank(id) && StrUtil.isBlank(code)){
            BizCodeEnum.PARAM_ERROR.newException();
        }
        DictEntity dictEntity = new DictEntity();
        dictEntity.setId(id);
        dictEntity.setCode(code);

        return this.success(this.service.getDict(dictEntity));
    }

    @PostMapping("/checkUnique")
    @Operation(summary = "校验code")
    public Response checkUnique(@Parameter(name ="dictEntity", description = "要进行校验的参数")
                                @RequestBody DictEntity dictEntity){
        boolean exist = (null != dictEntity && StrUtil.isNotBlank(dictEntity.getCode()))? this.service.checkUnique(dictEntity) : false;
        return exist? this.failure(): this.success();
    }

    @GetMapping("/del/{id}")
    @Operation(summary = "根据ID删除")
    public Response delDict(@Parameter(name = "id", description = "[字典ID]")
                            @PathVariable("id") String id){

        // 参数非空校验
        BizCodeEnum.NULL_ID.assertNotNull(id);

        this.service.delDict(id);
        return success();
    }
    @PostMapping("/edit")
    @Operation(summary = "新增/编辑字典分类")
    public Response editDict(@Parameter(name ="organization", description = "字典信息")
                             @RequestBody @Valid DictEntity dictEntity){
        this.service.edit(dictEntity);

        return this.success();
    }
}
