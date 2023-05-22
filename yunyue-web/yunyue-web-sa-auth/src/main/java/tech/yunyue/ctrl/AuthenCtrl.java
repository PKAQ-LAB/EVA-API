package tech.yunyue.ctrl;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.StpUtil;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.enums.BizCodeEnum;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import tech.yunyue.service.AuthenService;

import java.util.Map;

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

    /**
     * 登录认证
     */

    @PostMapping(value = "/login")
    @Operation(summary = "登录")
    public Response login(@RequestBody Map<String,String> params) {
        //验证码二次校验
        String captchaVerification = params.get("captchaVerification");
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(captchaVerification);
        ResponseModel response = captchaService.verification(captchaVO);
        if(!response.isSuccess()) {
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
     * @return
     */
    @PostMapping("/logout")
    @Operation(summary = "登出")
    public Response logout() {
        String userId = ThreadUserHelper.getUserId();

        // 注销access_token
        StpUtil.logout();
        // 注销refresh_token
        saTokenConfig.setTokenName(CommonConstant.REFRESH_TOKEN_KEY);
        // 用户access_token过期则不会有userId  从refresh_token中得到用户id
        if (!StringUtils.hasText(userId)) {
            userId = (String) StpUtil.getLoginId();
        }
        StpUtil.logout();
        saTokenConfig.setTokenName(CommonConstant.ACCESS_TOKEN_KEY); //改回来

        // 把缓存中的用户角色删掉
        StpUtil.getStpLogic().getSaTokenDao().delete(CommonConstant.REDIS_USER_ROLES_PREFIX_KEY+userId);
        return new Response().success(null,BizCodeEnum.LOGINOUT_SUCCESS);
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
    public Response refreshToken() {
        String refreshTokenId = "";
        //判断refresh_token是否有效
        try {
            saTokenConfig.setTokenName(CommonConstant.REFRESH_TOKEN_KEY);
            refreshTokenId = (String) StpUtil.getLoginId();
        }catch (NotLoginException e){
            BizCodeEnum.LOGIN_EXPIRED.newException();
        }

        String userId = (String)StpUtil.getExtra("userId");
        String account = (String)StpUtil.getExtra("account");
        String version = (String)StpUtil.getExtra("version");
        String device = StpUtil.getLoginDevice();
        //重新生成refresh_token
        StpUtil.login(refreshTokenId,SaLoginConfig.setExtra("userId", userId)
                .setExtra("account", account)
                .setExtra("version",version)
                .setDevice(device)
                .setTimeout(evaConfig.getJwt().getBravoTtl()));

        //重新生成access_token
        saTokenConfig.setTokenName(CommonConstant.ACCESS_TOKEN_KEY);
        StpUtil.login(userId, SaLoginConfig.setExtra("userId", userId)
                .setExtra("account", account)
                .setExtra("version",version)
                .setDevice(device));
//        var map = Map.of(CommonConstant.ACCESS_TOKEN_KEY, new_alpha,
//                CommonConstant.REFRESH_TOKEN_KEY, new_bravo);
        return new Response().success();
    }
}
