package io.nerv.config;

import io.nerv.core.util.SecurityHelper;
import io.nerv.properties.EvaConfig;
import io.nerv.security.mybatis.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis plus 配置
 * @author PKAQ
 */
@Configuration
public class MybatisPlusPermissionConfig {
    @Autowired
    private SecurityHelper securityHelper;

    @Autowired
    private EvaConfig evaConfig;
    /**
    * 分页插件
    * @return
    */
   @Bean
   public PermissionInterceptor permissionInterceptor() {
       PermissionInterceptor permissionInterceptor = new PermissionInterceptor(securityHelper);
       permissionInterceptor.setEvaConfig(evaConfig);
       return permissionInterceptor;
   }
}