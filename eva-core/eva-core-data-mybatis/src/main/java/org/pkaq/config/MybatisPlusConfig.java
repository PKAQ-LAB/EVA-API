package org.pkaq.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

/**
 * mybatis plus 配置
 *
 * @author PKAQ
 */
@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
public class MybatisPlusConfig {
    private final EvaConfig evaConfig;
    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 租户插件
        if (CommonConstant.MODE_SAAS.equalsIgnoreCase(evaConfig.getMode())){
            interceptor.addInnerInterceptor(new TenantLineInnerInterceptor());
        }

        //分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());


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
        properties.setProperty("Postgre", "postgre");
        databaseIdProvider.setProperties(properties);
        return databaseIdProvider;
    }
}