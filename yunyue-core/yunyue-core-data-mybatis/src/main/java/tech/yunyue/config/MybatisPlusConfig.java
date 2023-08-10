package tech.yunyue.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.handler.CompanyTenantLineHandler;
import tech.yunyue.interceptor.GlobalParamsInterceptor;

import java.util.Objects;
import java.util.Properties;

/**
 * mybatis plus 配置
 *
 * @author PKAQ
 */
@Configuration
@MapperScan("tech.yunyue.**.mapper")
@EnableTransactionManagement
public class MybatisPlusConfig {
    @Autowired
    CompanyTenantLineHandler tenantLineHandler;
    @Autowired
    EvaConfig evaConfig;
    @Autowired(required = false)
    DataPermissionHandler dataPermissionHandler;
    /**
     * 分页插件
     *
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //多租户插件
        if(evaConfig.getTenant().isEnable()){
            interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(tenantLineHandler));
            interceptor.addInnerInterceptor(new GlobalParamsInterceptor());
        }
        //数据权限插件
        if(Objects.nonNull(dataPermissionHandler)) {
            interceptor.addInnerInterceptor(new DataPermissionInterceptor(dataPermissionHandler));
        }
        //分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /**
     * 数据库配置
     *
     * @return 配置
     */
    @Bean
    public DatabaseIdProvider getDatabaseIdProvider() {
        DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        properties.setProperty("Oracle", "oracle");
        properties.setProperty("MySQL", "mysql");
        databaseIdProvider.setProperties(properties);
        return databaseIdProvider;
    }
}
