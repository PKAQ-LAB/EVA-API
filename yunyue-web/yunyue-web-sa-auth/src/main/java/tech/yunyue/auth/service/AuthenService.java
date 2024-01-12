package tech.yunyue.auth.service;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tech.yunyue.auth.domain.JwtUserDetail;
import tech.yunyue.auth.domain.JwtUserFactory;
import tech.yunyue.core.cache.util.RedisUtil;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.log.base.LoginlogEntity;
import tech.yunyue.core.log.util.LogHelper;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.util.json.JsonUtil;
import tech.yunyue.core.web.util.RequestUtil;

/**
 * 校验密码
 */
@Service
@AllArgsConstructor
public class AuthenService {
    private final SaTokenConfig saTokenConfig;
    private final EvaConfig evaConfig;
    private final RedisUtil redisUtil;
    private final JDBCService jdbcService;
    private final CacheManager cacheManager;
    private final LogHelper logHelper;

    /**
     * 登录
     */
    public Response additionalAuthenticationChecks(String username, String password) {
        // 判断锁定标记是否存在 存在就直接报错
        String key = CommonConstant.REDIS_USER_NO_LOGIN_KEY + username;
        if (StringUtils.hasLength(redisUtil.get(key))) {
            // 计算剩余分钟数
            Long minutes = redisUtil.getExpire(key) / 60;
            BizCodeEnum.LOGIN_FAIL_COUNT_LOCKED.newException(minutes + 1);
        }

        // 查询和校验数据库用户
        JwtUserDetail user = retrieveUser(username);
        boolean matches = BCrypt.checkpw(password.toLowerCase(), user.getPassword()) || BCrypt.checkpw(password.toUpperCase(), user.getPassword());
        if (!matches) {
            recordFail(user.getAccount(), user.getTel());
            BizCodeEnum.ACCOUNT_OR_PWD_ERROR.newException();
        }
        // 登录成功 删除记录失败记录的集合
        redisUtil.delete(CommonConstant.REDIS_USER_LOGIN_FAIL_KEY + user.getAccount());

        // 生成access_token 6小时
        HttpServletRequest request = (HttpServletRequest) SaHolder.getRequest().getSource();
        saTokenConfig.setTokenName(CommonConstant.ACCESS_TOKEN_KEY);
        var model = SaLoginConfig.setExtra("userId", user.getId())
                .setExtra("account", user.getAccount())
                .setExtra("version", RequestUtil.getVersion(request))
                .setDevice(RequestUtil.getDeivce(request));
        StpUtil.login(user.getId(), model);
        // 生成refresh_token 30天
        saTokenConfig.setTokenName(CommonConstant.REFRESH_TOKEN_KEY);
        StpUtil.login(user.getId(), model.setTimeout(evaConfig.getJwt().getBravoTtl()));
        saTokenConfig.setTokenName(CommonConstant.ACCESS_TOKEN_KEY); // 改回来

        // 将用户信息保存到缓存中  因为JwtUserDetail没有默认无参构造函数 无法序列化成对象 所以转为json string存
        cacheManager.getCache(CommonConstant.CACHE_USERDATA).put(CommonConstant.REDIS_USER_INFO_PREFIX_KEY + user.getId(), JsonUtil.toJson(user));
        // 登录日志
        LoginlogEntity loginlog = new LoginlogEntity();
        loginlog.setOperateDatetime(DateUtil.now())
                .setDevice(RequestUtil.getDeivce(request))
                .setVersion(RequestUtil.getVersion(request))
                .setOperator(user.getAccount())
                .setOperatorName(user.getName())
                .setOperateType("login")
                .setCreateId(user.getId())
                .setPostId(user.getPostId())
                .setOrgId(user.getDeptId())
                .setTenantId(user.getTenantId());
        logHelper.save(loginlog);
        return new Response().success(BizCodeEnum.LOGIN_SUCCESS_WELCOME, user.getAccount());
    }

    /**
     * 记录用户失败次数 并判断是否30分钟内连续失败5次
     */
    private void recordFail(String account, String tel) {
        synchronized (account.intern()) {
            Long now = System.currentTimeMillis();
            String key = CommonConstant.REDIS_USER_LOGIN_FAIL_KEY + account;
            // 当前失败时间从左入栈
            Long listSize = redisUtil.leftPush(key, now);
            int maxCount = evaConfig.getLoginFailureCount();
            // 集合的数量大等于5 且当前时间-往后数第五个时间 < 30分钟 则保存该用户限制登录标记
            if (listSize >= maxCount && now - (Long) redisUtil.popIndex(key, maxCount - 1) < evaConfig.getLoginFailureTime()) {
                redisUtil.setForTimeMIN(CommonConstant.REDIS_USER_NO_LOGIN_KEY + account, "true", evaConfig.getLoginLockTime());
                redisUtil.setForTimeMIN(CommonConstant.REDIS_USER_NO_LOGIN_KEY + tel, "true", evaConfig.getLoginLockTime());
                redisUtil.delete(key);
                BizCodeEnum.LOGIN_FAIL_COUNT_LOCKED.newException(evaConfig.getLoginLockTime());
            } else {
                // 手动删掉多余数据 保留4个
                redisUtil.listTrim(key, 0, maxCount - 2);
            }
        }
    }


    /**
     * 查询和校验数据库用户
     */
    private JwtUserDetail retrieveUser(String username) {

        // 数据中中查询
        JwtUserDetail user = loadUserByUsername(username);
        // 校验用户状态
        check(user);
        return user;
    }

    /**
     * 根据用户名在数据中查询用户和角色
     */
    private JwtUserDetail loadUserByUsername(String account) {
        if (StrUtil.isBlank(account)) {
            BizCodeEnum.PERMISSION_DENY.newException();
        }
        var userMap = this.jdbcService.loadUserByUsername(account);
        BizCodeEnum.ACCOUNT_NOT_EXIST.assertNotBlank(userMap);
        // 查询用户拥有的角色
        var roleMap = this.jdbcService.getRoleById(StrUtil.toStringOrNull(userMap.get("ID")));
        return JwtUserFactory.create(userMap, roleMap);
    }

    /**
     * 校验用户是否可用
     */
    private void check(JwtUserDetail user) {
        if (user.isAccountNonLocked()) {
            BizCodeEnum.ACCOUNT_LOCKED.newException();
        }
    }
}
