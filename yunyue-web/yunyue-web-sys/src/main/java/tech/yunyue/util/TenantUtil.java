package tech.yunyue.util;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tech.yunyue.core.cache.util.RedisUtil;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.util.json.JsonUtil;
import tech.yunyue.sys.user.entity.UserEntity;
import tech.yunyue.sys.user.service.UserService;

/**
 * 获取租户id工具类
 */
@Component
@RequiredArgsConstructor
public class TenantUtil {
    private final RedisUtil conRedisUtil;
    private final UserService conUserService;
    //@ConfigurationProperties会生成cglib代理 即有两个EvaConcifg类型的bean
    //默认根据beanName匹配 否则根据类型匹配 所以要么属性名叫evaConfig 要么加上@Qualifier注解
    private final EvaConfig evaConfig;

    private static RedisUtil  redisUtil;
    private static UserService  userService;
    private static EvaConfig  sEvaConfig;

    @PostConstruct
    public void init(){
        redisUtil = conRedisUtil;
        userService = conUserService;
        sEvaConfig = evaConfig;
    }

    /**
     * 获取集团id
     */
    public static String getTenantId(String uid) {
        UserEntity userEntity = getUserEntity(uid);
        return userEntity.getTenantId();
    }

    /**
     * 获取公司id
     */
    public static String getComTenantId(String uid) {
        UserEntity userEntity = getUserEntity(uid);
        return userEntity.getCompanyTenantId();
    }

    /**
     * 获取用户信息
     * @param uid
     * @return
     */
    public static UserEntity getUserEntity(String uid) {
        //平台用户登录不查询租户信息
        if(sEvaConfig.isPlatform()){
            return new UserEntity();
        }
        //从redis中获取
        var key = CommonConstant.REDIS_USER_INFO_PREFIX_KEY+uid;
        UserEntity userEntity = null;
        String userStr = redisUtil.get(key);
        if(!StringUtils.hasText(userStr)){
            synchronized (key.intern()) {
                if(!StringUtils.hasText(redisUtil.get(key))){
                    //redis没有就从数据库读取
                    userEntity = userService.getById(uid);
                    redisUtil.setForTimeMIN(key, JsonUtil.toJson(userEntity), sEvaConfig.getJwt().getBravoTtl() / 60);
                }
                return userEntity;
            }
        }
        return JSONUtil.toBean(userStr, UserEntity.class);
    }
}
