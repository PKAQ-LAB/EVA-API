package io.nerv.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * mybatis plus 配置
 * @author PKAQ
 */
@Configuration
@MapperScan("io.nerv.**.mapper")
@EnableTransactionManagement
public class MybatisPlusConfig {
    /**
    * 分页插件
    * @return
    */
   @Bean
   public PaginationInterceptor paginationInterceptor() {
      return new PaginationInterceptor();
   }
}