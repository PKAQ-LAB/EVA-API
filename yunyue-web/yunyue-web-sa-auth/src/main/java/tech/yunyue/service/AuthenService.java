package tech.yunyue.service;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.jwt.SaJwtUtil;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.log.base.BizLogEntity;
import tech.yunyue.core.log.base.BizLogSupporter;
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

    private final BizLogSupporter bizLogSupporter;

    private final EvaConfig evaConfig;

    /**
     * 登录
     *
     */
    public Response additionalAuthenticationChecks(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();

        String[] passwordArr = parameterMap.get("password");
        JwtUserDetail user = retrieveUser(parameterMap.get("username"),passwordArr);

        boolean matches = BCrypt.checkpw(passwordArr[0], user.getPassword());
        if (!matches) {
            BizCodeEnum.ACCOUNT_OR_PWD_ERROR.newException();
        }
        //生成access_token 6小时
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
        SaTokenDao dao = StpUtil.getStpLogic().getSaTokenDao();
        dao.setObject(CommonConstant.REDIS_USER_ROLES_PREFIX_KEY+user.getId(), new ArrayList<String>(user.getAuthorities().keySet()), evaConfig.getJwt().getBravoTtl());


        //登录日志
        Map<String, Object> map = new HashMap<>(2);
        map.put(CommonConstant.USER_KEY, user);
        map.put(CommonConstant.ACCESS_TOKEN_KEY, StpUtil.getTokenValue());
        BizLogEntity bizLogEntity = new BizLogEntity();
        bizLogEntity.setDescription(user.getAccount() + " 登录了系统")
                .setOperateDatetime(DateUtil.now())
                .setDevice(RequestUtil.getDeivce(request))
                .setVersion(RequestUtil.getVersion(request))
                .setOperator(user.getAccount())
                .setOperateType("login");
        log.info(bizLogEntity.toString());
        bizLogSupporter.save(bizLogEntity);

        return new Response().success(BizCodeEnum.LOGIN_SUCCESS_WELCOME, user.getUsername());
    }

    /**
     * 查询和校验数据库用户
     */
    private JwtUserDetail retrieveUser(String[] usernameArr, String[] passwordArr) {
        if(Objects.isNull(usernameArr) || usernameArr.length < 1 || Objects.isNull(passwordArr) || passwordArr.length < 1 ){
            BizCodeEnum.ACCOUNT_OR_PWD_ERROR.newException();
        }
        String username = usernameArr[0].trim();
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
