//package org.pkaq.sys.tenant.ctrl;
//
//import cn.hutool.core.text.CharSequenceUtil;
//import com.baomidou.mybatisplus.core.metadata.IPage;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.pkaq.core.enums.BizCodeEnum;
//import org.pkaq.core.mvc.ctrl.Ctrl;
//import org.pkaq.core.mvc.vo.Response;
//import org.pkaq.core.mvc.vo.SingleArray;
//import org.pkaq.sys.SYSCode;
//import org.pkaq.sys.tenant.bo.TenantAuthBo;
//import org.pkaq.sys.tenant.bo.TenantEditBo;
//import org.pkaq.sys.tenant.bo.TenantQueryBo;
//import org.pkaq.sys.tenant.bo.TenantStatusBo;
//import org.pkaq.sys.tenant.service.TenantService;
//import org.pkaq.sys.tenant.vo.TenantDetailVo;
//import org.pkaq.sys.tenant.vo.TenantLeftListVo;
//import org.pkaq.sys.tenant.vo.TenantListVo;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//
///**
// * 租户管理控制器
// *
// * @author 茂茂AdamEve
// */
//@Tag(name = "租户管理")
//@RestController
//@RequestMapping("/sys/tenant")
//@RequiredArgsConstructor
//public class TenantCtrl extends Ctrl {
//    private final TenantService service;
//
//    @PostMapping("/edit")
//    @Operation(summary = "新增/编辑记录")
//    public Response<Object> edit(@Parameter(name = "formdata", description = "租户对象")
//                                 @RequestBody @Validated TenantEditBo bo) {
//        this.service.edit(bo);
//        return this.success();
//    }
//
//    @GetMapping("/list")
//    @Operation(summary = "根据条件查询列表数据 ")
//    public Response<IPage<TenantListVo>> list(@Parameter(name = "ResourcesQueryBo", description = "请求参数")
//                                              TenantQueryBo queryBo) {
//        return success(this.service.list(queryBo));
//    }
//
//    @GetMapping("/get/{id}")
//    @Operation(summary = "根据ID获得租户信息")
//    public Response<TenantDetailVo> getRole(@Parameter(name = "id", description = "记录ID")
//                                            @PathVariable("id") String id) {
//        return this.success(this.service.get(id));
//    }
//
//
//    @PostMapping("/switchStatus")
//    @Operation(summary = "切换租户可用状态")
//    public Response<Object> switchStatus(@RequestBody @Validated TenantStatusBo bo) {
//        this.service.switchStatus(bo);
//        return success();
//    }
//
//    @PostMapping("/checkUnique")
//    @Operation(summary = "校验租户code/name唯一性")
//    public Response<Object> checkUnique(@Parameter(name = "organization", description = "要进行校验的参数")
//                                        @RequestBody TenantEditBo editBo) {
//        boolean exist = null != editBo && CharSequenceUtil.isNotBlank(editBo.getCode()) && this.service.checkUnique(editBo);
//        return exist ? failure(SYSCode.TENANT_COED_NAME_EXIST) : success();
//    }
//
//
//    @PostMapping("/del")
//    @Operation(summary = "根据ID删除/批量删除记录")
//    public Response<Object> del(@Parameter(name = "ids", description = "[记录ID]")
//                                @RequestBody SingleArray<String> ids) {
//        // 参数非空校验
//        BizCodeEnum.NULL_ID.assertNotNull(ids);
//        BizCodeEnum.NULL_ID.assertNotNull(ids.getParam());
//        this.service.delete(ids.getParam());
//        return this.success();
//    }
//
//    @PostMapping({"/saveAuth"})
//    @Operation(summary = "保存租户角色关系")
//    public Response<Object> saveAuth(@Parameter(name = "param")
//                                     @RequestBody @Validated TenantAuthBo authBo) {
//        this.service.saveAuth(authBo);
//        return success();
//    }
//
//    @GetMapping({"/listNoPage"})
//    @Operation(summary = "角色/用户/部门/岗位管理左侧租户列表")
//    public Response<List<TenantLeftListVo>> listNoPage() {
//        return success(this.service.listNoPage());
//    }
//
//}
