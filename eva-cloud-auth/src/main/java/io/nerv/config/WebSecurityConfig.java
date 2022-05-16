package io.nerv.config;

import cn.hutool.core.util.ArrayUtil;
import io.nerv.properties.EvaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

/**
 * @author PKAQ
 * 1.必须保证prePostEnabled开启 否则@PreAuthorize("hasRole('ROLE_ADMIN')")无效
 * 2.启用EnableConfigurationProperties以使ConfigurationProperties生效
 */
@Configuration
@Primary
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final static String RSA_KEY = "/rsa/publicKey";
    @Autowired
    private EvaConfig evaConfig;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors()
                .and()
                // 关闭csrf 由于使用的是JWT，我们这里不需要csrf
                .csrf().disable()
                //允许加载iframe内容 X-Frame-Options
                .headers().frameOptions().disable()
                .xssProtection().block(true)
                .and()
                // 适配IE
                .addHeaderWriter(new StaticHeadersWriter("P3P", "CP='CAO IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT'"))
                .and()
                // 基于token，所以不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 获取公钥的接口无需授权
                .antMatchers(RSA_KEY).permitAll()
                // 任何请求都需要授权，注意顺序 从上至下
                .anyRequest().authenticated();

        // 匿名访问拥有的角色
        httpSecurity.anonymous().authorities("ROLE_ANONYMOUS");

        // disable page caching
        httpSecurity
                .headers()
                .frameOptions().sameOrigin()  // required to set for H2 else H2 Console will be blank.
                .cacheControl();
    }

    @Override
    public void configure(WebSecurity web) {
        var ws = web
                .ignoring()
                // allow anonymous resource requests
                .and()
                .ignoring()
                .antMatchers(
                        HttpMethod.GET,
                        "/",
                        "/static/**",
                        "/*.html",
                        "/*.xls",
                        "/*.xlsx",
                        "/*.doc",
                        "/*.docx",
                        "/*.pdf",
                        "/favicon.ico",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/webjars/**",
                        "/ureport/**",
                        "/swagger-resources/**",
                        "/*/api-docs"
                );

        //TODO 替换成eva.config.webstatic
        if (ArrayUtil.isNotEmpty("webstatic")){
            ws.antMatchers("webstatic");
        }
    }

    /**
     * 如果不配置 SpringBoot 会自动配置一个 AuthenticationManager 覆盖掉内存中的用户
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
