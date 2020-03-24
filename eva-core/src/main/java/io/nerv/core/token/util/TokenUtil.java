package io.nerv.core.token.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.token.jwt.JwtUtil;
import io.nerv.core.util.RedisUtil;
import io.nerv.core.util.RequestUtil;
import io.nerv.properties.EvaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class TokenUtil {
    @Autowired
    private EvaConfig evaConfig;

    @Autowired
    private RequestUtil requestUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    private Cache tokenCache;

    public TokenUtil(CacheManager cacheManager) {
        this.tokenCache = cacheManager.getCache(CommonConstant.CACHE_TOKEN);;
    }

    // 构造token缓存的value
    public Map<String, Object> buildCacheValue(HttpServletRequest request, String uid,  String token){
        return Map.of("device", requestUtil.getDeivce(request),
                      "version", requestUtil.getVersion(request),
                      "issuedAt", jwtUtil.getIssuedAt(token),
                      "loginTime", LocalDateTime.now(),
                      "account", uid,
                      "token", token);
    }

    // 持久化 token
    public void saveToken(String key, Object value){
        this.tokenCache.put(key, value);
    }


    /***
     * 获取所有的token
     * @return
     */
    public Map<?, ?> getAllToken(){
        if (this.tokenCache instanceof CaffeineCache){
            CaffeineCache caffeineCache = (CaffeineCache)this.tokenCache;
            return caffeineCache.getNativeCache().asMap();
        }

        if (this.tokenCache instanceof RedisCache){
            return redisUtil.getPureAll(CommonConstant.CACHE_TOKEN);
        }
        return null;
    }
    /**
     * 获取token缓存
     * @param uid
     * @return
     */
    public Cache.ValueWrapper getToken(String uid){
        return this.tokenCache.get(uid);
    }
    /**
     * 从缓存中清除token
     * @param key
     */
    public void removeToken(String key){
        this.tokenCache.evict(key);
    }
    /**
     * 获取token
     * @param request
     * @return
     */
    public String getToken(HttpServletRequest request){
        String authToken = null;

        var authHeader = request.getHeader(evaConfig.getJwt().getHeader());

        if(null != ServletUtil.getCookie(request, CommonConstant.TOKEN_KEY)){
            authToken = ServletUtil.getCookie(request, CommonConstant.TOKEN_KEY).getValue();
        } else  if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith(evaConfig.getJwt().getTokenHead())) {
            authToken = authHeader.substring(evaConfig.getJwt().getTokenHead().length());
        }

        return authToken;
    }
}
