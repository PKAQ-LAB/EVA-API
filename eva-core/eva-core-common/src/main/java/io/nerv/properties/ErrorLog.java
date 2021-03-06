package io.nerv.properties;

import lombok.Data;

/**
 * 错误日志配置读取类
 * @author: S.PKAQ
 * @Datetime: 2018/9/27 21:06
 */
@Data
public class ErrorLog {
    /**
     * 是否启用
     */
    private boolean  enabled = false;

}
