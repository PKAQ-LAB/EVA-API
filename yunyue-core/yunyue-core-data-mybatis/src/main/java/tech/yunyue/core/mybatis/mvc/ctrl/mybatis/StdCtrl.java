package tech.yunyue.core.mybatis.mvc.ctrl.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import tech.yunyue.core.annotation.NoRepeatSubmit;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.enums.ResponseEnumm;
import tech.yunyue.core.mvc.ctrl.Ctrl;
import tech.yunyue.core.mvc.vo.PageBo;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.mvc.vo.SingleArray;
import tech.yunyue.core.mybatis.mvc.entity.mybatis.StdEntity;
import tech.yunyue.core.mybatis.mvc.service.mybatis.StdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Controller 基类
 *
 * @author S.PKAQ
 */
@Getter
public abstract class StdCtrl<T extends StdService<? extends BaseMapper<E>,E>, E extends StdEntity> extends Ctrl {
    @Autowired
    protected T service;

    @PostMapping("/del")
    @Operation(summary = "删除记录", description = "根据ID删除/批量删除记录")
    @NoRepeatSubmit
    public Response del(@Parameter(name = "ids", description = "[记录ID]")
                        @RequestBody SingleArray<String> ids) {

        BizCodeEnum.NULL_ID.assertNotNull(ids);
        BizCodeEnum.NULL_ID.assertNotNull(ids.getParam());

        this.service.delete(ids.getParam());
        return success(null, ResponseEnumm.DELETE_SUCCESS);
    }

    @PostMapping("/edit")
    @Operation(summary = "新增记录", description = "新增/编辑记录")
    @NoRepeatSubmit
    public Response save(@Parameter(name = "formdata", description = "模型对象")
                         @RequestBody E entity) {
        this.service.merge(entity);
        return success(entity, ResponseEnumm.SAVE_SUCCESS);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询", description = "列表查询")
    @NoRepeatSubmit
    public Response<IPage<E>> list(@Parameter(name = "condition", description = "模型对象")
                         PageBo<E> page) {
        return this.success(this.service.listPage(page));
    }

    @GetMapping("/listAll")
    @Operation(summary = "查询全部", description = "列表查询 无分页")
    @NoRepeatSubmit
    public Response<List<E>> listAll(@Parameter(name = "condition", description = "模型对象")
                            E entity) {
        return this.success(this.service.list(entity));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID查询", description = "根据ID获得记录信息")
    @NoRepeatSubmit
    public Response<E> get(@Parameter(name = "id", description = "记录ID")
                        @PathVariable("id") String id) {
        return this.success(this.service.getById(id));
    }
}
