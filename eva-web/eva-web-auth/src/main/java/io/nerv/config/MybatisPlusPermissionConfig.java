package io.nerv.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import io.nerv.core.util.SecurityHelper;
import io.nerv.properties.EvaConfig;
import io.nerv.security.mybatis.MybatisPlusDataPermissionHandler;
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

    @Autowired
    private MybatisPlusInterceptor mybatisPlusInterceptor;
    /**
    * 数据权限插件
    * @return
    */
   @Bean
   public void permissionInterceptor() {

       DataPermissionInterceptor dataPermissionInterceptor = new DataPermissionInterceptor();

       MybatisPlusDataPermissionHandler myDataPermissionHandler = new MybatisPlusDataPermissionHandler(securityHelper);
                               myDataPermissionHandler.setEvaConfig(evaConfig);

       dataPermissionInterceptor.setDataPermissionHandler(myDataPermissionHandler);

       mybatisPlusInterceptor.addInnerInterceptor(dataPermissionInterceptor);
   }
}