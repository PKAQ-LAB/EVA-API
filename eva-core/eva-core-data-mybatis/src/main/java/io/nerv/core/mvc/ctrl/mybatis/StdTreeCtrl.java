package io.nerv.core.mvc.ctrl.mybatis;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.nerv.core.annotation.NoRepeatSubmit;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.enums.ResponseEnumm;
import io.nerv.core.exception.ParamException;
import io.nerv.core.mvc.ctrl.Ctrl;
import io.nerv.core.mvc.entity.mybatis.StdTreeEntity;
import io.nerv.core.mvc.service.mybatis.StdTreeService;
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
public abstract class StdTreeCtrl<T extends StdTreeService, E extends StdTreeEntity> extends Ctrl {
    @Autowired
    protected T service;


    @PostMapping("/checkUnique")
    @Operation(summary = "校验code唯一性")
    public Response checkUnique(@Parameter(name ="entity", description = "要进行校验的参数")
                                @RequestBody E entity){
        boolean exist = (null != entity && StrUtil.isNotBlank(entity.getCode()))? this.service.count(entity) > 0 : false;
        return exist? failure(BizCodeEnum.ORG_CODE_EXIST): success();
    }

    @GetMapping("/list")
    @Operation(summary = "根据实体类属性获取相应的树结构 ")
    public Response listOrgByAttr(@Parameter(name = "entity", description= "{key: value}") E entity){
        return success(this.service.lisTree(entity));
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID获取节点信息")
    public Response getOrg(@Parameter(name = "id", description = "节点ID")
                           @PathVariable("id") String id){
        return success(this.service.get(id));
    }

    @PostMapping("/del")
    @Operation(summary = "根据ID删除/批量删除节点")
    //@PreAuthorize("hasRole('ADMIN')")
    public Response delOrg(@Parameter(name = "ids", description = "[节点ID]")
                           @RequestBody SingleArray<String> ids){
        // 参数非空校验
        if (null == ids || CollectionUtil.isEmpty(ids.getParam())){
            throw new ParamException(locale("param_id_notnull"));
        }
        // 判断上级节点是否还有其它叶子 如果没有把 isleaf属性改为false
        return this.service.delete(ids.getParam());
    }

    @PostMapping("/edit")
    @Operation(summary = "编辑节点信息")
    public Response editOrg(@Parameter(name ="entity", description = "节点信息")
                            @RequestBody E entity){
        this.service.edit(entity);
        return success();
    }

    @PostMapping("/sort")
    @Operation(summary = "排序节点信息")
    public Response sortOrg(@Parameter(name = "entity", description = "{id,orders}")
                            @RequestBody E[] switchObj){
        this.service.swtich(switchObj);
        return success();
    }

    @PostMapping("/switchStatus")
    @Operation(summary = "切换节点可用状态")
    public Response switchStatus(@Parameter(name = "id", description = "节点Id")
                                 @RequestBody E entity){
        this.service.changeStatus(entity);
        return success();
    }
}
