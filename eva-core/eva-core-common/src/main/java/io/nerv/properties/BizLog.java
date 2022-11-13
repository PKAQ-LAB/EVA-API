package io.nerv.properties;

import lombok.Data;

/**
 * 业务日志配置读取类
 * @author: S.PKAQ
 */
@Data
public class BizLog {
    /**
     * 是否启用
     */
    private boolean  enabled = false;
    /**
     * 实现类
     */
    private String impl = "";

}
