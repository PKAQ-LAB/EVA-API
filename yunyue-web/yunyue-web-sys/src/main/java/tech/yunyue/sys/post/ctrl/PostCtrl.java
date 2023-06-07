package tech.yunyue.sys.post.ctrl;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.yunyue.sys.post.bo.PostEditBo;
import tech.yunyue.sys.post.bo.PostQueryBo;
import tech.yunyue.sys.post.errorcode.SYSCode;
import tech.yunyue.sys.post.service.PostService;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.mvc.ctrl.Ctrl;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.mvc.vo.SingleArray;


/**
 * 岗位管理
 *
 * @author : dmz
 */
@Tag(name = "岗位管理")
@RestController
@RequestMapping("/sys/post")
@RequiredArgsConstructor
public class PostCtrl extends Ctrl {

    /**
     * 岗位管理Service
     */
    private final PostService postService;

    @PostMapping("/checkUnique")
    @Operation(summary = "校验code/name唯一性")
    public Response checkUnique(@Parameter(name = "postEditBo", required = true, description = "岗位管理新增/编辑/唯一校验请求参数")
                                @RequestBody PostEditBo postEditBo) {
        // 参数校验
        if (StrUtil.isAllBlank(postEditBo.getCode(), postEditBo.getTitle())) {
            return failure(SYSCode.MISS_CODE_OR_NAME);
        }
        boolean exists = this.postService.checkUnique(postEditBo);
        return exists ? success() : failure(SYSCode.DUPLICATE_CODE_OR_NAME);
    }

    @GetMapping("/list")
    @Operation(summary = "根据条件查询岗位管理列表数据")
    public Response list(@Parameter(name = "query", description = "请求参数")
                         PostQueryBo query) {

        return success(this.postService.list(query));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获取岗位管理信息")
    public Response get(@Parameter(name = "id", description = "岗位管理信息ID")
                        @PathVariable(name = "id") String id) {
        return this.success(this.postService.get(id));
    }

    @PostMapping("/edit")
    @Operation(summary = "新增/编辑岗位管理信息")
    public Response edit(@Parameter(name = "edit", description = "编辑")
                         @RequestBody @Validated PostEditBo bo) {
        this.postService.edit(bo);
        return this.success();
    }

    @PostMapping("/del")
    @Operation(summary = "根据ID删除")
    public Response del(@Parameter(name = "ids", description = "[ids]")
                        @RequestBody SingleArray<String> ids) {
        // 参数非空校验
        BizCodeEnum.NULL_ID.assertNotNull(ids.getParam());
        this.postService.del(ids.getParam());
        return success();
    }

}
