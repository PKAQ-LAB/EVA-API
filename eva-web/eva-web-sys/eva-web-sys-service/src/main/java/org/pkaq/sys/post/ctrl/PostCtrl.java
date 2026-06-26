package org.pkaq.sys.post.ctrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.log.annotation.BizLog;
import org.pkaq.core.log.base.BizLogCodes;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.util.StrUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.post.bo.PostAoeBo;
import org.pkaq.sys.post.bo.PostQueryBo;
import org.pkaq.sys.post.bo.PostSortBo;
import org.pkaq.sys.post.service.PostService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 岗位管理 Controller
 *
 * @author dmz
 */
@Tag(name = "岗位管理")
@RestController
@RequestMapping("/sys/post")
@RequiredArgsConstructor
public class PostCtrl extends Ctrl {

    private final PostService postService;

    @PostMapping("/checkUnique")
    @Operation(summary = "校验 code / title 在同 pid 下是否唯一")
    public Response<Object> checkUnique(@Parameter(name = "bo", required = true, description = "岗位唯一性校验请求参数")
                                        @RequestBody PostAoeBo bo) {
        if (StrUtils.isAllBlank(bo.getCode(), bo.getTitle())) {
            return failure(SysCodes.MISS_CODE_OR_NAME);
        }
        boolean exists = this.postService.checkUnique(bo);
        return exists ? failure(SysCodes.DUPLICATE_CODE_OR_NAME) : success();
    }

    @GetMapping("/list")
    @Operation(summary = "获取岗位树")
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询了岗位树[{0}]", args = {"param:0"})
    public Response<Object> list(@Parameter(name = "query", description = "请求参数") PostQueryBo query) {
        return success(this.postService.list(query));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据 ID 获取岗位详情")
    @BizLog(operateType = BizLogCodes.QUERY, description = "查询了岗位信息[{0}]", args = {"param:0"})
    public Response<Object> get(@Parameter(name = "id", description = "岗位ID")
                                @PathVariable("id") Long id) {
        return success(this.postService.get(id));
    }

    @PostMapping("/edit")
    @Operation(summary = "新增/编辑岗位")
    @BizLog(operateType = BizLogCodes.EDIT, description = "编辑了岗位信息[{0}]", args = {"param:0"})
    public Response<Object> edit(@Parameter(name = "bo", description = "岗位对象")
                                 @RequestBody @Validated PostAoeBo bo) {
        this.postService.edit(bo);
        return success();
    }

    @PostMapping("/del")
    @Operation(summary = "根据 ID 批量删除")
    @BizLog(operateType = BizLogCodes.DELETE, description = "删除了岗位[{0}]", args = {"param:0"})
    public Response<Object> del(@Parameter(name = "ids", description = "[岗位ID]")
                                @RequestBody SingleArray<Long> ids) {
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());
        this.postService.del(ids.getParam());
        return success();
    }

    @PostMapping("/sort")
    @Operation(summary = "同级拖拽排序")
    @BizLog(operateType = BizLogCodes.UPDATE, description = "调整了岗位顺序[{0}]", args = {"param:0"})
    public Response<Object> sort(@Parameter(name = "bo", description = "{id, oldSort, newSort}")
                                 @RequestBody PostSortBo bo) {
        this.postService.sort(bo);
        return success();
    }

    @PostMapping("/switch")
    @Operation(summary = "切换冻结状态（级联子节点）")
    @BizLog(operateType = BizLogCodes.UPDATE, description = "切换了岗位冻结状态[{0}]", args = {"param:0"})
    public Response<Object> switchFrozen(@Parameter(name = "ids", description = "[岗位Id]")
                                         @RequestBody SingleArray<Long> ids) {
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());
        this.postService.switchFrozen(ids);
        return success();
    }
}
