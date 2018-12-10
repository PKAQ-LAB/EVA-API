package org.pkaq.core.mvc.ctrl;

import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import org.pkaq.core.enums.HttpCodeEnum;
import org.pkaq.core.exception.ParamException;
import org.pkaq.core.mvc.entity.PureBaseEntity;
import org.pkaq.core.mvc.service.BaseService;
import org.pkaq.core.mvc.util.Response;
import org.pkaq.core.mvc.util.SingleArray;
import org.pkaq.core.util.I18NHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller 基类
 * Datetime: 2018/3/5 23:37
 * @author S.PKAQ
 */
@Getter
public abstract class BaseCtrl<T extends BaseService, E extends PureBaseEntity> {
    @Autowired
    protected T service;
    @Autowired
    protected I18NHelper i18NHelper;

    @PostMapping("del")
    @ApiOperation(value = "根据ID删除/批量删除记录",response = Response.class)
    public Response del(@ApiParam(name = "ids", value = "[记录ID]")
                        @RequestBody SingleArray<String> ids){

        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }

        return success(this.service.delete(ids.getParam()));
    }

    @PostMapping("edit")
    @ApiOperation(value = "新增/编辑记录",response = Response.class)
    public Response save(@ApiParam(name ="formdata", value = "模型对象")
                         @RequestBody E entity){
        this.service.merge(entity);
        return success(entity.getId());
    }

    @GetMapping("list")
    @ApiOperation(value = "列表查询",response = Response.class)
    public Response list(@ApiParam(name ="condition", value = "模型对象")
                                 E entity, Integer pageNo){
        return this.success(this.service.listPage(entity, pageNo));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据ID获得记录信息", response = Response.class)
    public Response getRole(@ApiParam(name = "id", value = "记录ID")
                            @PathVariable("id") String id){
        return this.success(this.service.getById(id));
    }
    /**
     * 根据编码获取国际化字符串
     * @param code
     * @return
     */
    protected String locale(String code){
        return this.i18NHelper.getMessage(code);
    }
    /**
     * 操作成功
     * @return
     */
    protected Response success(){
        return new Response().success(null);
    }
    /**
     * 返回成功结果
     * @param data
     * @return
     */
    protected Response success(Object data){
        return new Response().success(data);
    }
    /**
     * 返回失败结果
     * @param failCode
     * @return
     */
    protected Response failure(int failCode){
        return new Response().failure(failCode);
    }

    /**
     * 返回失败结果
     * @return
     */
    protected Response failure() { return new Response().failure(HttpCodeEnum.RULECHECK_FAILED.getIndex());}
}
