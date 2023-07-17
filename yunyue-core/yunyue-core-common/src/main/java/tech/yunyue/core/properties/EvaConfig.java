package tech.yunyue.core.properties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 业务日志配置读取类
 *
 * @author PKAQ
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "eva")
@EnableConfigurationProperties(EvaConfig.class)
public class EvaConfig {
    /**
     * 是否开启国际化
     */
    private boolean i18n;
    /**
     * 是否启用重复提交判断
     **/
    private boolean norepeatCheck;
    /**
     * 业务日志配置
     **/
    private BizLog bizlog;
    /**
     * 业务日志配置
     **/
    private ErrorLog errorLog;
    /**
     * 文件上传配置
     **/
    private Upload upload;
    /**
     * jwt配置
     **/
    private Jwt jwt;
    /**
     * cookie 配置
     **/
    private Cookie cookie;
    /**
     * 访问鉴权配置
     **/
    private Security security;
    /**
     * 数据权限配置
     **/
    private DataPermission dataPermission;
    /**
     * 资源权限配置
     **/
    private ResourcePermission resourcePermission;
    /**
     * 是否启用license 授权机制
     **/
    private License license;
    /**
     * 缓存配置
     **/
    private Cache cache;

    /**
     * 租户配置
     */
    private Tenant tenant;

    /**
     *  是否允许同一账号并发登录 (为true时允许一起登录, 为false时新登录挤掉旧登录)
     */
    private Boolean concurrent = true;

    /**
     * 多少分钟内连续失败则限制用户登录[毫秒]
     */
    private long loginFailureTime = 30 * 60 * 1000;

    /**
     * 失败次数
     */
    private int loginFailureCount = 5;

    /**
     * 限制用户登录分钟数
     */
    private long loginLockTime = 30;

    public BizLog getBizlog() {
        return null == this.bizlog ? new BizLog() : bizlog;
    }

    public ErrorLog getErrorLog() {
        return null == this.errorLog ? new ErrorLog() : errorLog;
    }

    public Upload getUpload() {
        return null == this.upload ? new Upload() : upload;
    }

    public Jwt getJwt() {
        return null == this.jwt ? new Jwt() : jwt;
    }

    public Cookie getCookie() {
        return null == this.cookie ? new Cookie() : cookie;
    }

    public DataPermission getDataPermission() {
        return null == this.dataPermission ? new DataPermission() : dataPermission;
    }

    public ResourcePermission getResourcePermission() {
        return null == resourcePermission ? new ResourcePermission() : resourcePermission;
    }

    public License getLicense() {
        return null == license ? new License() : license;
    }

    public Cache getCache() {
        return null == cache ? new Cache() : cache;
    }

    public Tenant getTenant() { return null == tenant ? new Tenant() : tenant; }
}
