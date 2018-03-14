package org.pkaq.core.mvc;

import lombok.Getter;
import org.pkaq.core.util.I18NHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller 基类
 * Datetime: 2018/3/5 23:37
 * @author S.PKAQ
 */
@Getter
public abstract class BaseCtrl<T extends BaseService> {
    @Autowired
    protected T service;
    @Autowired
    protected I18NHelper i18NHelper;
}
