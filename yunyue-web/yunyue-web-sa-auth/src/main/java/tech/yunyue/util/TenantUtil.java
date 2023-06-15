package tech.yunyue.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tech.yunyue.auth.domain.JwtUserDetail;
import tech.yunyue.auth.domain.JwtUserFactory;
import tech.yunyue.auth.service.JDBCService;
import tech.yunyue.core.cache.util.RedisUtil;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.properties.EvaConfig;
import cn.hutool.json.JSONUtil;

import java.util.Collections;

/**
 * 获取租户id工具类
 */
@Component
@RequiredArgsConstructor
public class TenantUtil {
    private final RedisUtil conRedisUtil;
    private final JDBCService conJdbcService;
    //@ConfigurationProperties会生成cglib代理 即有两个EvaConcifg类型的bean
    //默认根据beanName匹配 否则根据类型匹配 所以要么属性名叫evaConfig 要么加上@Qualifier注解
    private final EvaConfig evaConfig;

    private static RedisUtil  redisUtil;
    private static EvaConfig  sEvaConfig;
    private static JDBCService jdbcService;

    @PostConstruct
    public void init(){
        redisUtil = conRedisUtil;
        sEvaConfig = evaConfig;
        jdbcService = conJdbcService;
    }

    /**
     * 获取集团id
     */
    public static String getTenantId(String uid) {
        JwtUserDetail userEntity = getUserEntity(uid);
        return userEntity.getTenantId();
    }

    /**
     * 获取公司id
     */
    public static String getComTenantId(String uid) {
        JwtUserDetail userEntity = getUserEntity(uid);
        return userEntity.getCompanyTenantId();
    }

    /**
     * 获取用户信息
     * @param uid
     * @return
     */
    public static JwtUserDetail getUserEntity(String uid) {
        //从redis中获取
        var key = CommonConstant.REDIS_USER_INFO_PREFIX_KEY+uid;
        JwtUserDetail userEntity = null;
        String userStr = redisUtil.get(key);
        if(!StringUtils.hasText(userStr)){
            synchronized (key.intern()) {
                if(!StringUtils.hasText(redisUtil.get(key))){
                    //redis没有就从数据库读取
                    userEntity = JwtUserFactory.create(jdbcService.loadUserById(uid), Collections.emptyList());
                    redisUtil.setForTimeMIN(key, JSONUtil.toJsonStr(userEntity), sEvaConfig.getJwt().getBravoTtl() / 60);
                }
                return userEntity;
            }
        }
        return JSONUtil.toBean(userStr, JwtUserDetail.class);
    }
}
