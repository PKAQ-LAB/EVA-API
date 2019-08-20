package io.nerv.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 业务日志配置读取类
 * @author: S.PKAQ
 * @Datetime: 2018/9/27 21:06
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "eva")
@EnableConfigurationProperties(EvaConfig.class)
public class EvaConfig {
    /**业务日志配置 **/
    private BizLog bizlog;

    /**业务日志配置 **/
    private ErrorLog errorLog;

    /** 文件上传配置 **/
    private Upload upload;

    /** jwt配置 **/
    private Jwt jwt;

    /** cookie 配置 **/
    private Cookie cookie;

    public BizLog getBizlog() {
        return null == this.bizlog? new BizLog() : bizlog;
    }

    public ErrorLog getErrorLog() {
        return null == this.errorLog? new ErrorLog() : errorLog;
    }

    public Upload getUpload() {
        return null == this.upload? new Upload() : upload;
    }

    public Jwt getJwt() {
        return null == this.jwt? new Jwt() : jwt;
    }

    public Cookie getCookie() {
        return null == this.cookie? new Cookie() : cookie;
    }
}
