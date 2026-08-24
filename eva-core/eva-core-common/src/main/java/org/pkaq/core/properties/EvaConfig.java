package org.pkaq.core.properties;

import lombok.Data;
import org.pkaq.core.constant.CommonConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 业务配置读取类
 *
 * @author PKAQ
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "eva")
@EnableConfigurationProperties(EvaConfig.class)
public class EvaConfig {
    /**
     * 运行模式：standalone=非租户系统，platform=平台管理端，saas=租户端。
     */
    private String mode = CommonConstant.MODE_STANDALONE;

    /**
     * 微服务访问配置。
     */
    private Cloud cloud;

    /**
     * 多租户模式专属配置
     */
    private Tenant tenant;
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
     * 错误日志配置
     **/
    private ErrorLog errorLog;
    /**
     * 分页配置
     */
    private Page page = new Page();
    /**
     * 文件上传配置
     **/
    private File upload;
    /**
     * jwt配置
     **/
    private Jwt jwt;
    /**
     * cookie 配置
     **/
    private Cookie cookie;
    /**
     * 鉴权配置
     **/
    private Auth auth;
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

    public BizLog getBizlog() {
        return null == this.bizlog ? new BizLog() : bizlog;
    }

    public ErrorLog getErrorLog() {
        return null == this.errorLog ? new ErrorLog() : errorLog;
    }

    public File getUpload() {
        return null == this.upload ? new File() : upload;
    }

    public Jwt getJwt() {
        return null == this.jwt ? new Jwt() : jwt;
    }

    public Cookie getCookie() {
        return null == this.cookie ? new Cookie() : cookie;
    }

    public Auth getAuth() {
        return null == this.auth ? new Auth() : auth;
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

    public Tenant getTenant() {
        return null == tenant ? new Tenant() : tenant;
    }

    public Cloud getCloud() {
        return null == cloud ? new Cloud() : cloud;
    }

    public boolean isStandaloneMode() {
        return CommonConstant.MODE_STANDALONE.equalsIgnoreCase(mode)
                || CommonConstant.MODE_SINGLETON.equalsIgnoreCase(mode);
    }

    public boolean isPlatformMode() {
        return CommonConstant.MODE_PLATFORM.equalsIgnoreCase(mode);
    }

    public boolean isSaasMode() {
        return CommonConstant.MODE_SAAS.equalsIgnoreCase(mode);
    }
}
