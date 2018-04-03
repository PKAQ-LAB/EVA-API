package org.pkaq.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Druid连接池相关配置.
 * @author PKAQ
 */
@Configuration
public class DruidConfiguration {
    /**
     * druid日志配置.
     * @return StatFilter
     */
    @Bean(name = "stat-filter")
    @Lazy
    public StatFilter statFilter(){
        final long slowSqlMilels = 3*1000L;
        StatFilter statFilter = new StatFilter();
        statFilter.setSlowSqlMillis(slowSqlMilels);
        statFilter.setLogSlowSql(true);
        statFilter.setMergeSql(true);
        return statFilter;
    }

    /**
     * druid监控配置.
     * @return WebStatFilter
     */
    @Bean(name = "DruidWebStatFilter")
    public WebStatFilter webStatFilter(){
        WebStatFilter webStatFilter = new WebStatFilter();
        String exclusion = "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*";
        webStatFilter.isExclusion(exclusion);
        webStatFilter.setProfileEnable(true);
        webStatFilter.setSessionStatEnable(false);
        return webStatFilter;
    }

    /**
     * 注册druid的servlet.
     * @return ServletRegistrationBean
     */
    @Bean
    public ServletRegistrationBean servletRegistrationBean(){
        return new ServletRegistrationBean(new StatViewServlet() , "/druid/*");
    }
}
