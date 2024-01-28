package tech.yunyue.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * 业务日志配置读取类
 *
 * @author PKAQ
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "eva")
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
     * 系统日志配置
     **/
    private SysLog syslog;
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
    private Boolean concurrent = false;

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

    public SysLog getSyslog() {
        return Optional.ofNullable(syslog).orElse(new SysLog());
    }


    public Upload getUpload() {
        return Optional.ofNullable(upload).orElse(new Upload());
    }

    public Jwt getJwt() {
        return Optional.ofNullable(jwt).orElse(new Jwt());
    }

    public Cookie getCookie() {
        return Optional.ofNullable(cookie).orElse(new Cookie());
    }

    public DataPermission getDataPermission() {
        return Optional.ofNullable(dataPermission).orElse(new DataPermission());
    }

    public ResourcePermission getResourcePermission() {
        return Optional.ofNullable(resourcePermission).orElse(new ResourcePermission());
    }

    public Cache getCache() {
        return Optional.ofNullable(cache).orElse(new Cache());
    }

    public Tenant getTenant() { return Optional.ofNullable(tenant).orElse(new Tenant()); }

}
