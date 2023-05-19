package tech.yunyue.service;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tech.yunyue.core.cache.util.RedisUtil;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.util.BizLogUtil;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.web.util.RequestUtil;
import tech.yunyue.domain.JwtUserDetail;
import tech.yunyue.domain.JwtUserFactory;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.sys.user.entity.UserEntity;
import tech.yunyue.sys.user.mapper.UserMapper;

import java.util.*;

/**
 * 校验密码
 */
@Slf4j
@Component
@AllArgsConstructor
public class AuthenService {
    private final UserMapper userMapper;

    private final SaTokenConfig saTokenConfig;
    private final EvaConfig evaConfig;

    private final RedisUtil redisUtil;

    /**
     * 登录
     *
     */
    public Response additionalAuthenticationChecks(String username, String password) {
        // 判断锁定标记是否存在 存在就直接报错
        String key = CommonConstant.REDIS_USER_NO_LOGIN_KEY + username;
        if(StringUtils.hasLength(redisUtil.get(key))){
            //计算剩余分钟数
            Long minutes =  redisUtil.getExpire(CommonConstant.REDIS_USER_NO_LOGIN_KEY + username) / 60 ;
            BizCodeEnum.LOGIN_FAIL_COUNT_LOCKED.newException(minutes+1);
        }

        //查询和校验数据库用户
        JwtUserDetail user = retrieveUser(username);
        boolean matches = BCrypt.checkpw(password, user.getPassword());
        if (!matches) {
            recordFail(user.getUsername());
            BizCodeEnum.ACCOUNT_OR_PWD_ERROR.newException();
        }
        //登录成功 删除记录失败记录的集合
        redisUtil.delete(CommonConstant.REDIS_USER_LOGIN_FAIL_KEY + user.getUsername());

        //生成access_token 6小时
        HttpServletRequest request = (HttpServletRequest)SaHolder.getRequest().getSource();
        saTokenConfig.setTokenName(CommonConstant.ACCESS_TOKEN_KEY);
        StpUtil.login(user.getId(), SaLoginConfig.setExtra("userId", user.getId())
                .setExtra("account", user.getUsername())
                .setExtra("version",RequestUtil.getVersion(request))
                .setDevice(RequestUtil.getDeivce(request)));
        //生成refresh_token 30天
        saTokenConfig.setTokenName(CommonConstant.REFRESH_TOKEN_KEY);
        StpUtil.login(CommonConstant.REFRESH_TOKEN_KEY+":"+user.getId(),SaLoginConfig.setExtra("userId", user.getId())
                .setExtra("account", user.getUsername())
                .setExtra("version",RequestUtil.getVersion(request))
                .setDevice(RequestUtil.getDeivce(request))
                .setTimeout(evaConfig.getJwt().getBravoTtl()));
        saTokenConfig.setTokenName(CommonConstant.ACCESS_TOKEN_KEY); //改回来

        // 将用户角色保存到redis中
        SaTokenDao redisDao = StpUtil.getStpLogic().getSaTokenDao();
        redisDao.setObject(CommonConstant.REDIS_USER_ROLES_PREFIX_KEY+user.getId(), new ArrayList<String>(user.getAuthorities().keySet()), evaConfig.getJwt().getBravoTtl());


        //登录日志
        BizLogEntity bizLogEntity = new BizLogEntity();
        bizLogEntity.setDescription(user.getAccount() + " 登录了系统")
                .setOperateDatetime(DateUtil.now())
                .setDevice(RequestUtil.getDeivce(request))
                .setVersion(RequestUtil.getVersion(request))
                .setOperator(user.getAccount())
                .setOperateType("login");
        log.info(bizLogEntity.toString());
        BizLogUtil.sava(bizLogEntity);

        return new Response().success(BizCodeEnum.LOGIN_SUCCESS_WELCOME, user.getUsername());
    }

    /**
     * 记录用户失败次数 并判断是否30分钟内连续失败5次
     * @param username 用户名
     */
    private void recordFail(String username) {
        synchronized (username.intern()) {
            Long now = System.currentTimeMillis();
            String key = CommonConstant.REDIS_USER_LOGIN_FAIL_KEY + username;
            //当前失败时间从左入栈
            Long listSize = redisUtil.leftPush(key,now);
            int maxCount = evaConfig.getLoginFailureCount();
            // 集合的数量大等于5 且当前时间-往后数第五个时间 < 30分钟 则保存该用户限制登录标记
            if (listSize >= maxCount && now - (Long)redisUtil.popIndex(key,maxCount-1) < evaConfig.getLoginFailureTime()){
                redisUtil.setForTimeMIN(CommonConstant.REDIS_USER_NO_LOGIN_KEY + username,"true",evaConfig.getLoginLockTime());
                redisUtil.delete(key);
                BizCodeEnum.LOGIN_FAIL_COUNT_LOCKED.newException(evaConfig.getLoginLockTime());
            }else{
                //手动删掉多余数据 保留4个
                redisUtil.listTrim(key,0,maxCount-2);
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
        UserEntity user = new UserEntity();
        user.setAccount(account);
        user.setTel(account);
        user.setEmail(account);
        user = userMapper.getUserWithRole(user);
        return JwtUserFactory.create(user);
    }

    /**
     * 校验用户是否可用
     */
    private void check(JwtUserDetail user) {
        BizCodeEnum.ACCOUNT_NOT_EXIST.assertNotNull(user);
        if (!user.isAccountNonLocked()) {
            BizCodeEnum.ACCOUNT_LOCKED.assertNotNull(user);
        }
    }
}
