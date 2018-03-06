package org.pkaq.core.mvc;

import lombok.Getter;
import org.pkaq.core.util.I18NHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller 基类
 * Author: S.PKAQ
 * Datetime: 2018/3/5 23:37
 */
@Getter
public class BaseCtrl {
    @Autowired
    private I18NHelper i18NHelper;
}
