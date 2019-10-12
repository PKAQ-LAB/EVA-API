package io.nerv.config;

import io.nerv.properties.EvaConfig;
import io.nerv.security.mybatis.PermissionInterceptor;
import io.nerv.security.util.SecurityUtil;
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
    private SecurityUtil securityUtil;

    /**
    * 分页插件
    * @return
    */
   @Bean
   public PermissionInterceptor permissionInterceptor() {
      return new PermissionInterceptor(securityUtil);
   }
}