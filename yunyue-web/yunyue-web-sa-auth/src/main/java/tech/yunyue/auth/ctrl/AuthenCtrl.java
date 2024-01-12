package tech.yunyue.auth.ctrl;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import tech.yunyue.auth.service.AuthenService;
import tech.yunyue.auth.service.JDBCService;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.event.BizEvent;
import tech.yunyue.core.log.base.LoginlogEntity;
import tech.yunyue.core.log.util.LogHelper;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.threaduser.ThreadUser;
import tech.yunyue.core.util.json.JsonUtil;
import tech.yunyue.core.web.util.RequestUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Slf4j
@Tag(name = "登录管理")
public class AuthenCtrl {
    private final AuthenService authenService;
    private final EvaConfig evaConfig;
    private final SaTokenConfig saTokenConfig;
    private final CaptchaService captchaService;
    private final ApplicationEventPublisher publisher;
    private final JDBCService jdbcService;
    private final LogHelper logHelper;

    /**
     * 登录认证
     */

    @PostMapping(value = "/login")
    @Operation(summary = "登录")
    public Response login(@RequestBody Map<String, String> params) {
        //验证码二次校验
        String captchaVerification = params.get("captchaVerification");
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(captchaVerification);
        ResponseModel response = captchaService.verification(captchaVO);
        if (!response.isSuccess() && "prod".equals(SpringUtil.getActiveProfile())) {
            BizCodeEnum.LOGIN_CAPTCHA_FAIL.newException();
            log.error("验证失败：" + response.getRepMsg());
        }

        //校验账号密码
        String username = params.get("username");
        String password = params.get("password");
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            BizCodeEnum.ACCOUNT_OR_PWD_ERROR.newException();
        }
        return authenService.additionalAuthenticationChecks(username, password);
    }


    /**
     * 退出登录需要需要登录的一点思考：
     * 1、如果不需要登录，那么在调用接口的时候就需要把token传过来，且系统不校验token有效性，此时如果系统被攻击，不停的大量发送token，最后会把redis充爆
     * 2、如果调用退出接口必须登录，那么系统会调用token校验有效性，refresh_token通过参数传过来加入黑名单
     * 综上：选择调用退出接口需要登录的方式
     *
     * @return
     */
    @PostMapping("/logout")
    @Operation(summary = "登出")
    public Response<String> logout(HttpServletRequest request) {
        String userId = null;
        try {
            // 通过refresh_token得到用户id
            saTokenConfig.setTokenName(CommonConstant.REFRESH_TOKEN_KEY);
            userId = (String) StpUtil.getLoginId();
        } catch (SaTokenException ignored) {
            // 用户登录失效时，会抛异常 不处理
        } finally {
            saTokenConfig.setTokenName(CommonConstant.ACCESS_TOKEN_KEY); //改回来
        }
        // 发布踢出用户事件 登出用户
        if (Objects.nonNull(userId)) {

            publisher.publishEvent(new BizEvent(CommonConstant.KICK_USER_EVENT, Map.of(
                    "ids", Collections.singletonList(userId),
                    CommonConstant.DEVICE, RequestUtil.getDeivce(request)
            )));

            //登出日志
            ThreadUser currentUser = JSONUtil.toBean(this.jdbcService.loadUserById(userId), ThreadUser.class);
            LoginlogEntity loginlog = new LoginlogEntity()
                    .setOperateDatetime(DateUtil.now())
                    .setDevice(RequestUtil.getDeivce(request))
                    .setVersion(RequestUtil.getVersion(request))
                    .setOperator(currentUser.getAccount())
                    .setOperatorName(currentUser.getName())
                    .setOperateType("logout")
                    .setCreateId(userId)
                    .setPostId(currentUser.getPostId())
                    .setOrgId(currentUser.getDeptId())
                    .setTenantId(currentUser.getTenantId());
            logHelper.save(loginlog);
        }
        return new Response<String>().success("", BizCodeEnum.LOGINOUT_SUCCESS);
    }

    /**
     * 使用refresh token 换取 access token
     * 1. 签发新的 access_token
     * 2. 删除老的 access_token
     * 3. 签发新的 refreshToken
     * 4. 删除老的 refreshToken
     *
     * @return
     */
    @PostMapping("/getAlpha")
    @Operation(summary = "刷新token")
    public Response refreshToken(HttpServletResponse response) throws IOException {
        String userId = "";
        //判断refresh_token是否有效
        try {
            saTokenConfig.setTokenName(CommonConstant.REFRESH_TOKEN_KEY);
            userId = (String) StpUtil.getLoginId();
        } catch (NotLoginException e) {
            // token过期 返回401 用户重新登录
            try (PrintWriter printWriter = response.getWriter()) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setCharacterEncoding("UTF-8");
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                printWriter.write(JsonUtil.toJson(new Response().failure(BizCodeEnum.LOGIN_EXPIRED)));
                printWriter.flush();
            }
            saTokenConfig.setTokenName(CommonConstant.ACCESS_TOKEN_KEY); //改回来
            return null;
        }

        String account = (String) StpUtil.getExtra("account");
        String version = (String) StpUtil.getExtra("version");
        String device = StpUtil.getLoginDevice();
        //重新生成refresh_token
        StpUtil.login(userId, SaLoginConfig.setExtra("userId", userId)
                .setExtra("account", account)
                .setExtra("version", version)
                .setDevice(device)
                .setTimeout(evaConfig.getJwt().getBravoTtl()));

        //重新生成access_token
        saTokenConfig.setTokenName(CommonConstant.ACCESS_TOKEN_KEY);
        StpUtil.login(userId, SaLoginConfig.setExtra("userId", userId)
                .setExtra("account", account)
                .setExtra("version", version)
                .setDevice(device));
        return new Response<>().success();
    }
}
