package org.pkaq.sys.dict.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.util.StrUtils;
import org.pkaq.sys.dict.bo.DictAoeBo;
import org.pkaq.sys.dict.service.DictService;
import org.pkaq.sys.dict.vo.DictViewVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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

    /**
     * 根据类型从缓存中获取可选叶子字典项銆?     *
     * @param code 字典类型
     * @return 字典项     */
    @GetMapping("/query/{code}")
    @Operation(summary = "根据类型从缓存中获取可选叶子字典项")
    public Response<Map<String, String>> query(@Parameter(name = "code", description = "字典类型")
                                               @PathVariable(name = "code") String code) {
        return this.success(this.service.queryDict(code));
    }

    /**
     * 获取完整字典树。     *
     * @return 字典树     */
    @GetMapping("/list")
    @Operation(summary = "获取完整字典树")
    public Response<List<DictViewVo>> listDict() {
        return this.success(this.service.listDict());
    }

    /**
     * 获取指定类型的字典树銆?     *
     * @param type 字典类型
     * @return 字典树     */
    @GetMapping("/tree/{type}")
    @Operation(summary = "获取指定类型的字典树")
    public Response<List<DictViewVo>> treeByType(@Parameter(name = "type", description = "字典类型")
                                                 @PathVariable("type") String type) {
        return this.success(this.service.listDictByType(type));
    }

    /**
     * 校验字典值是否为可选叶子节点。     *
     * @param type 字典类型
     * @param value 字典值     * @return 校验结果
     */
    @GetMapping("/validate/{type}/{value}")
    @Operation(summary = "校验字典值是否为可选叶子节点")
    public Response<Boolean> validateLeaf(@Parameter(name = "type", description = "字典类型")
                                          @PathVariable("type") String type,
                                          @Parameter(name = "value", description = "字典值")
                                          @PathVariable("value") String value) {
        return this.success(this.service.validateLeaf(type, value));
    }

    /**
     * 根据 ID 获取字典节点銆?     *
     * @param id 字典节点 ID
     * @return 字典详情
     */
    @GetMapping("/get/{id}")
    @Operation(summary = "根据 ID 获取字典节点")
    public Response<DictViewVo> getDict(@Parameter(name = "id", description = "字典节点 ID")
                                        @PathVariable("id") Long id) {
        CommonCodes.NULL_ID.assertNotNull(id);
        DictAoeBo bo = new DictAoeBo();
        bo.setId(id);
        return this.success(this.service.getDict(bo));
    }

    /**
     * 根据编码获取字典节点。     *
     * @param code 字典编码
     * @return 字典详情
     */
    @GetMapping("/get/type/{code}")
    @Operation(summary = "根据编码获取字典节点")
    public Response<DictViewVo> getDictByCode(@Parameter(name = "code", description = "字典编码")
                                              @PathVariable("code") String code) {
        if (StrUtils.isBlank(code)) {
            CommonCodes.PARAM_ERROR.newException();
        }
        DictAoeBo bo = new DictAoeBo();
        bo.setCode(code);
        bo.setType(code);
        bo.setPid(0L);
        return this.success(this.service.getDict(bo));
    }

    /**
     * 校验同级字典编码唯一性。     *
     * @param bo 字典参数
     * @return 响应结果
     */
    @PostMapping("/checkUnique")
    @Operation(summary = "校验 code")
    public Response<Object> checkUnique(@Parameter(name = "dictEntity", description = "要进行校验的参数")
                                        @RequestBody DictAoeBo bo) {
        boolean exist = bo != null && StrUtils.isNotBlank(bo.getCode()) && this.service.checkUnique(bo);
        return exist ? this.failure() : this.success();
    }

    /**
     * 根据 ID 删除字典节点。     *
     * @param id 字典节点 ID
     * @return 响应结果
     */
    @GetMapping("/del/{id}")
    @Operation(summary = "根据ID删除")
    public Response<Object> delDict(@Parameter(name = "id", description = "[字典节点 ID]")
                                    @PathVariable("id") Long id) {
        CommonCodes.NULL_ID.assertNotNull(id);

        this.service.delDict(id);
        return success();
    }

    /**
     * 新增或编辑字典节点。     *
     * @param dictEntity 字典参数
     * @return 响应结果
     */
    @PostMapping("/edit")
    @Operation(summary = "新增/编辑字典节点")
    public Response<Object> editDict(@Parameter(name = "dictEntity", description = "字典信息")
                                     @RequestBody @Valid DictAoeBo dictEntity) {
        this.service.edit(dictEntity);
        return this.success();
    }

    /**
     * 切换字典冻结状态。     *
     * @param param 字典 ID 集合
     * @return 响应结果
     */
    @PostMapping("/switch")
    @Operation(summary = "锁定/解锁")
    public Response<Object> switchFrozen(@Parameter(name = "param", description = "字典[id]")
                                         @RequestBody SingleArray<Long> param) {
        CommonCodes.NULL_ID.assertNotNull(param.getParam());

        this.service.switchFrozen(param);
        return success();
    }
}
