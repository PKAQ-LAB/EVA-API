package io.nerv.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

//@Configuration
//@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Autowired
    private AuthConfig authConfig;

    @Autowired
    private TokenStore tokenStore;
 
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
        		.anyRequest().permitAll()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                //.antMatchers(authConfig.getAnonymous()).permitAll()
                .anyRequest().authenticated();
                //.and().httpBasic()
               // .and().csrf().disable();
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId("resourceId")
                 .tokenStore(tokenStore);
    }
    
    
}
