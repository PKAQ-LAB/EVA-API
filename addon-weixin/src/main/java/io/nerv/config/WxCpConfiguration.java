package io.nerv.config;

import com.google.common.collect.Maps;
import io.nerv.weixin.properties.WxCpProperties;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.WxCpUserService;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.api.impl.WxCpUserServiceImpl;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;
import me.chanjar.weixin.cp.message.WxCpMessageRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 企业微信配置类
 */
@Configuration
@EnableConfigurationProperties(WxCpProperties.class)
public class WxCpConfiguration {

    private WxCpProperties properties;

    private static Map<Integer, WxCpMessageRouter> routers = Maps.newHashMap();

    private static Map<Integer, WxCpService> cpServices = Maps.newHashMap();

    @Autowired
    public WxCpConfiguration(WxCpProperties properties) {
        this.properties = properties;
    }

    public static Map<Integer, WxCpMessageRouter> getRouters() {
        return routers;
    }

    public static WxCpService getCpService(Integer agentId) {
        return cpServices.get(agentId);
    }

    @PostConstruct
    public void initServices() {
        cpServices = this.properties.getAppConfigs().stream().map(a -> {
            WxCpDefaultConfigImpl configStorage = new WxCpDefaultConfigImpl();
            configStorage.setCorpId(this.properties.getCorpId());
            configStorage.setAgentId(a.getAgentId());
            configStorage.setCorpSecret(a.getSecret());
            configStorage.setToken(a.getToken());
            configStorage.setAesKey(a.getAesKey());
            WxCpServiceImpl service = new WxCpServiceImpl();
            service.setWxCpConfigStorage(configStorage);
            return service;
        }).collect(Collectors.toMap(service -> service.getWxCpConfigStorage().getAgentId(), a -> a));
    }

    @Bean
    public WxCpService wxCpService(){
        WxCpDefaultConfigImpl configStorage = new WxCpDefaultConfigImpl();
        configStorage.setCorpId(this.properties.getCorpId());
        configStorage.setCorpSecret(this.properties.getContactSecret());
        configStorage.setToken("user");
        configStorage.setAesKey("nerv");

        WxCpServiceImpl service = new WxCpServiceImpl();
        service.setWxCpConfigStorage(configStorage);

        return service;
    }

    @Bean
    public WxCpUserService wxCpUserService(){
        return new WxCpUserServiceImpl(this.wxCpService());
    }
}