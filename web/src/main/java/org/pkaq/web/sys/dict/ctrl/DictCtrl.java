package org.pkaq.web.sys.dict.ctrl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.pkaq.core.mvc.BaseCtrl;
import org.pkaq.core.util.Response;
import org.pkaq.web.sys.dict.service.DictService;
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

    public Response getDict(){
        return null;
    }

    public Response editDict(){
        return null;
    }

    public Response delDict(){
        return null;
    }
}
