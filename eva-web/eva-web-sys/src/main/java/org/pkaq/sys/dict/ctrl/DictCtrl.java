package org.pkaq.sys.dict.ctrl;

import cn.hutool.core.text.CharSequenceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.enums.BizCodeEnum;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.cache.DictCacheHelper;
import org.pkaq.sys.dict.entity.DictEntity;
import org.pkaq.sys.dict.service.DictService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 字典管理控制器
 * @author PKAQ
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/sys/dictionary")
@RequiredArgsConstructor
public class DictCtrl extends Ctrl {
    private final DictService service;

    private final LocaleResolver localeResolver;

    private final DictCacheHelper dictCacheHelper;

    public static void main(String[] args) {
        BizCodeEnum.NULL_PARAM_ID.assertNotNull(null, "角色");
    }


    @GetMapping({"/query/{code}"})
    @Operation(summary = "根据 code 从缓存中获取字典项")
    public Response<Map<String, String>> query(@Parameter(name = "code", description = "字典分类ID")
                          @PathVariable(name = "code", required = false) String code) {
        return this.success(dictCacheHelper.get(code));
    }

    @GetMapping("/list")
    @Operation(summary = "获取字典分类列表")
    public Response<List<DictEntity>> listDict() {
        return this.success(this.service.listDict());
    }

    @GetMapping({"/get/{id}", "/get/type/{code}"})
    @Operation(summary = "根据ID获取字典")
    public Response<DictEntity> getDict(@Parameter(name = "id", description = "字典分类ID")
                            @PathVariable(name = "id", required = false) String id,
                            @Parameter(name = "code", description = "类型编码")
                            @PathVariable(value = "code", required = false) String code) {
        // 参数校验
        if (CharSequenceUtil.isBlank(id) && CharSequenceUtil.isBlank(code)) {
            BizCodeEnum.PARAM_ERROR.newException();
        }
        DictEntity dictEntity = new DictEntity();
        dictEntity.setId(id);
        dictEntity.setCode(code);

        return this.success(this.service.getDict(dictEntity));
    }

    @PostMapping("/checkUnique")
    @Operation(summary = "校验code")
    public Response<Object> checkUnique(@Parameter(name = "dictEntity", description = "要进行校验的参数")
                                @RequestBody DictEntity dictEntity) {
        boolean exist = null != dictEntity && CharSequenceUtil.isNotBlank(dictEntity.getCode()) && this.service.checkUnique(dictEntity);
        return exist ? this.failure() : this.success();
    }

    @GetMapping("/del/{id}")
    @Operation(summary = "根据ID删除")
    public Response<Object> delDict(@Parameter(name = "id", description = "[字典ID]")
                            @PathVariable("id") String id) {

        // 参数非空校验
        BizCodeEnum.NULL_ID.assertNotNull(id);

        this.service.delDict(id);
        return success();
    }

    @PostMapping("/edit")
    @Operation(summary = "新增/编辑字典分类")
    public Response<Object> editDict(@Parameter(name = "organization", description = "字典信息")
                                     @RequestBody @Valid DictAoeBo dictEntity) {

        this.service.edit(dictEntity);

        return this.success();
    }
}
