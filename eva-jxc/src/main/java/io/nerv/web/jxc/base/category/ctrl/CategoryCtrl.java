package io.nerv.web.jxc.base.category.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import io.nerv.core.exception.ParamException;
import io.nerv.core.mvc.ctrl.mybatis.PureBaseCtrl;
import io.nerv.core.mvc.util.Response;
import io.nerv.core.mvc.util.SingleArray;
import io.nerv.web.jxc.base.category.entity.CategoryEntity;
import io.nerv.web.jxc.base.category.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

/**
 * 分类管理controller
 * @author: S.PKAQ
 * @Datetime: 2018/3/28 19:20
 */
@Api( tags = "分类管理")
@RestController
@RequestMapping("/pdos/category")
public class CategoryCtrl extends PureBaseCtrl<CategoryService> {
    @GetMapping("/list")
    @ApiOperation(value = "根据实体类属性获取相应的分类树 ", response = Response.class)
    public Response list(@ApiParam(name = "condition", value= "分类A") String condition){
        return success(this.service.listCategory(condition, null));
    }

    @PostMapping("/checkUnique")
    @ApiOperation(value = "校验code唯一性",response = Response.class)
    public Response checkUnique(@ApiParam(name ="category", value = "要进行校验的参数")
                                @RequestBody CategoryEntity category){
        boolean exist = this.service.checkUnique(category);
        return exist? failure(): success();
    }

    @PostMapping("edit")
    @ApiOperation(value = "新增/编辑记录",response = Response.class)
    public Response save(@ApiParam(name ="formdata", value = "分类对象")
                         @RequestBody CategoryEntity entity){
        return success( this.service.editCategory(entity));
    }

    @PostMapping("/del")
    @ApiOperation(value = "根据ID删除/批量删除记录",response = Response.class)
    public Response del(@ApiParam(name = "ids", value = "[记录ID]")
                        @RequestBody SingleArray<String> ids){

        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }

        return this.service.deleteCategory(ids.getParam());
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据ID获得记录信息", response = Response.class)
    public Response getRole(@ApiParam(name = "id", value = "记录ID")
                            @PathVariable("id") String id){
        return this.success(this.service.getById(id));
    }

    @PostMapping("/switch")
    @ApiOperation(value = "切换可用状态", response = Response.class)
    public Response switchStatus(@ApiParam(name = "id", value = "分类Id")
                                 @RequestBody CategoryEntity module){
        this.service.updateCategory(module);
        return success();
    }
}
