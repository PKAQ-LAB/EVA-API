package org.pkaq.sys.post.ctrl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.mvc.bo.SingleArray;
import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.pkaq.core.util.StrUtils;
import org.pkaq.sys.SysCodes;
import org.pkaq.sys.post.bo.PostAoeBo;
import org.pkaq.sys.post.bo.PostQueryBo;
import org.pkaq.sys.post.service.PostService;
import org.pkaq.sys.post.vo.PostDetailVo;
import org.pkaq.sys.post.vo.PostListVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public Response<Object> checkUnique(@Parameter(name = "bo", required = true, description = "岗位管理新增/编辑/唯一校验请求参数")
                                        @RequestBody PostAoeBo bo) {
        // 参数校验
        if (StrUtils.isAllBlank(bo.getCode(), bo.getTitle())) {
            return failure(SysCodes.MISS_CODE_OR_NAME);
        }
        // checkUnique 返回 true = 已存在重复
        boolean exists = this.postService.checkUnique(bo);
        return exists ? failure(SysCodes.DUPLICATE_CODE_OR_NAME) : success();
    }

    @GetMapping("/list")
    @Operation(summary = "根据条件查询岗位管理列表数据")
    public Response<IPage<PostListVo>> list(@Parameter(name = "query", description = "请求参数")
                                            PostQueryBo query) {

        return success(this.postService.list(query));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获取岗位管理信息")
    public Response<PostDetailVo> get(@Parameter(name = "id", description = "岗位管理信息ID")
                                      @PathVariable(name = "id") String id) {
        return this.success(this.postService.get(id));
    }

    @PostMapping("/edit")
    @Operation(summary = "新增/编辑岗位管理信息")
    public Response<Object> edit(@Parameter(name = "edit", description = "编辑")
                                 @RequestBody @Validated PostAoeBo bo) {
        this.postService.edit(bo);
        return this.success();
    }

    @PostMapping("/del")
    @Operation(summary = "根据ID删除")
    public Response<Object> del(@Parameter(name = "ids", description = "[ids]")
                                @RequestBody SingleArray<String> ids) {
        // 参数非空校验
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());
        this.postService.del(ids.getParam());
        return success();
    }

    @PostMapping("/switch")
    @Operation(summary = "切换冻结状态")
    public Response<Object> switchFrozen(@Parameter(name = "ids", description = "[岗位Id]")
                                         @RequestBody SingleArray<Long> ids) {
        CommonCodes.NULL_ID.assertNotNull(ids.getParam());
        this.postService.switchFrozen(ids);
        return success();
    }

}
