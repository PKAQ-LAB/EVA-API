package org.pkaq.web.sys.dict.ctrl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pkaq.core.mvc.BaseCtrl;
import org.pkaq.core.util.Response;
import org.pkaq.web.sys.dict.service.DictItemService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 字典项管理控制器
 * @author: S.PKAQ
 * @Datetime: 2018/3/22 0:35
 */
@Api( description = "字典项管理")
@RestController
@RequestMapping("/dictItem")
public class DictItemCtrl extends BaseCtrl<DictItemService> {

    @RequestMapping("/del/{id}")
    @ApiOperation(value = "根据ID删除",response = Response.class)
    public Response delete(@ApiParam(name = "id", value = "[字典项ID]")
                           @PathVariable("id") String id){
        this.service.delDictItem(id);
        return success();
    }
}
