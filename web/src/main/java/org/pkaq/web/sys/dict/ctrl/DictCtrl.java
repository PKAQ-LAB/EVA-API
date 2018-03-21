package org.pkaq.web.sys.dict.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pkaq.core.enums.HttpCodeEnum;
import org.pkaq.core.exception.ParamException;
import org.pkaq.core.mvc.BaseCtrl;
import org.pkaq.core.util.Response;
import org.pkaq.core.util.SingleArray;
import org.pkaq.web.sys.dict.entity.DictEntity;
import org.pkaq.web.sys.dict.service.DictService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 字典管理控制器
 * @author: S.PKAQ
 * @Datetime: 2018/3/15 0:11
 */
@Api( description = "字典管理")
@RestController
@RequestMapping("/dict")
public class DictCtrl extends BaseCtrl<DictService>{

    @RequestMapping("/list")
    @ApiOperation(value = "获取字典分类列表",response = Response.class)
    public Response listDict(){
        return new Response().success(this.service.listDict());
    }

    @RequestMapping({"/get/{id}", "/get/type/{code}"})
    @ApiOperation(value = "根据ID获取字典",response = Response.class)
    public Response getDict(@ApiParam(name = "id", value = "字典分类ID")
                            @PathVariable("id") String id,
                            @ApiParam(name = "code", value = "类型编码")
                            @PathVariable(value = "code", required = false) String code){
        // 参数校验
        if (StrUtil.isBlank(id) && StrUtil.isBlank(code)){
            return failure(HttpCodeEnum.REQEUST_FAILURE.getIndex());
        }
        DictEntity dictEntity = new DictEntity();
        dictEntity.setId(id);
        dictEntity.setCode(code);

        return success(this.service.getDict(dictEntity));
    }

    @RequestMapping("/del")
    @ApiOperation(value = "根据ID删除/批量删除组织",response = Response.class)
    public Response delDict(@ApiParam(name = "ids", value = "[组织ID]")
                                 @RequestBody SingleArray<String> ids){
        // 参数非空校验
        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }
        this.service.delDict(ids.getParam());
        return success(null);
    }

    public Response editDict(){
        return null;
    }
}
