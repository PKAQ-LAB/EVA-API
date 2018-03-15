package org.pkaq.web.sys.dict.ctrl;

import io.swagger.annotations.Api;
import org.pkaq.core.mvc.BaseCtrl;
import org.pkaq.core.util.Response;
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
public class DictCtrl extends BaseCtrl{
    public Response listDict(){
        return null;
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
