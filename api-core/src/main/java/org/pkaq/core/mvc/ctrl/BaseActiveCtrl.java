package org.pkaq.core.mvc.ctrl;

import lombok.Getter;
import org.pkaq.core.enums.HttpCodeEnum;
import org.pkaq.core.mvc.service.BaseActiveRecordService;
import org.pkaq.core.util.I18NHelper;
import org.pkaq.core.mvc.util.Response;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller 基类
 * Datetime: 2018/3/5 23:37
 * @author S.PKAQ
 */
@Getter
public abstract class BaseActiveCtrl<T extends BaseActiveRecordService> {
    @Autowired
    protected T service;
    @Autowired
    protected I18NHelper i18NHelper;

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
