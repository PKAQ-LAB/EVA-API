package io.nerv.log.ctrl;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.token.util.CacheTokenUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/monitor/log/online")
@Api(tags = "获取在线用户")
public class OnlineUserCtrl {
    @Autowired
    private CacheTokenUtil tokenUtil;

    @GetMapping("/list")
    @ApiOperation(value = "获取在线用户列表", response = Response.class)
    public Response list(@ApiParam(name ="uid", value = "查询固定用户") String account){
        Response response = new Response();
        if (StrUtil.isNotBlank(account)){
            Object obj = this.tokenUtil.getToken(account);

            List list = new ArrayList(1);

            if (null != obj){
                list.add(obj);
            }

            response.setData(list);
        } else {
            response.setData(this.tokenUtil.getAllToken().values());
        }
        return response.success();
    }

    @GetMapping("/offline")
    @ApiOperation(value = "踢掉一个用户", response = Response.class)
    public Response offlineUser(@ApiParam(name ="uid", value = "要踢掉的用户id")  String uid){
        this.tokenUtil.removeToken(uid);
        return new Response().success();
    }
}
