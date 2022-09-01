package io.nerv.core.mvc.ctrl.mybatis;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.nerv.core.enums.ResponseEnumm;
import io.nerv.core.exception.ParamException;
import io.nerv.core.mvc.ctrl.Ctrl;
import io.nerv.core.mvc.service.mybatis.ActiveBaseService;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.mvc.vo.SingleArray;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller 基类
 * @author S.PKAQ
 */
@Getter
public abstract class ActiveBaseCtrl<T extends ActiveBaseService, E extends Model> extends Ctrl {
    @Autowired
    protected T service;

    @PostMapping("del")
    @Operation(summary = "删除记录", description = "根据ID删除/批量删除记录")
    public Response del(@Parameter(name = "ids", description = "[记录ID]")
                        @RequestBody SingleArray<String> ids){

        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }
        this.service.delete(ids.getParam());
        return success(this.service.listPage(null, 1), ResponseEnumm.DELETE_SUCCESS.getName());
    }

    @PostMapping("edit")
    @Operation(summary = "新增记录",description = "新增/编辑记录")
    public Response save(@Parameter(name ="formdata", description = "模型对象")
                         @RequestBody E entity){
        this.service.merge(entity);
        return success(entity, ResponseEnumm.SAVE_SUCCESS.getName());
    }

    @GetMapping("list")
    @Operation(summary = "分页查询",description = "列表查询")
    public Response list(@Parameter(name ="condition", description = "模型对象")
                                 E entity, Integer pageNo, Integer pageSize){
        return this.success(this.service.listPage(entity, pageNo, pageSize));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID查询", description = "根据ID获得记录信息")
    public Response get(@Parameter(name = "id", description = "记录ID")
                            @PathVariable("id") String id){
        return this.success(this.service.getById(id));
    }
}
