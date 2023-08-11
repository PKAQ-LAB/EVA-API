package tech.yunyue.core.properties;

import lombok.Data;

/**
 * 系统日志配置读取类
 *
 * @author: S.PKAQ
 */
@Data
public class SysLog {
    /**
     * 实现类
     */
    private String impl = "";
    //是否推送到mq
    private boolean mqEnabled = false;

}
