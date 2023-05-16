package tech.yunyue.core.auth.config;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaFilterAuthStrategy;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tech.yunyue.core.constant.CommonConstant;
import tech.yunyue.core.exception.BizException;
import tech.yunyue.core.mvc.vo.Response;
import tech.yunyue.core.properties.EvaConfig;
import tech.yunyue.core.properties.Jwt;


/**
 * [Sa-Token 权限认证] 全局配置类
 */
@Configuration
public class SaTokenConfigure{
    @Autowired
    EvaConfig evaConfig;
    @Autowired
    SaFilterAuthStrategy saFilterAuthStrategy;

    /**
     * 注册 [Sa-Token全局过滤器]
     * 过滤器中抛出的异常无法进入全局@ExceptionHandler
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        // 设置自定义权限匹配策略  目前的匹配策略可以使用 暂不需要自定义
//        SaStrategy.me.setHasElement(new RouterAuthStrategy());
        return new SaServletFilter()
                // 指定 拦截路由 与 放行路由
                .addInclude("/**")
                .addExclude(excludeStaticPaths())
                // 认证函数: 每次请求执行
                .setAuth(saFilterAuthStrategy)
                // 异常处理函数：每次认证函数发生异常时执行此函数
                .setError(e -> {
                    // 设置错误返回格式为JSON
                    SaHolder.getResponse().setHeader("Content-Type", "application/json; charset=utf-8");
                    if (e instanceof BizException) {
                        return JSONUtil.toJsonStr( new Response().failure(((BizException) e).getBizCode()) );
                    }else{
                        return JSONUtil.toJsonStr( new Response().failure("500",e.getMessage()) );
                    }
                })
                // 前置函数：在每次认证函数之前执行
                .setBeforeAuth(r -> {
                    // ---------- 设置一些安全响应头 ----------
                    SaHolder.getResponse()
                            // 允许指定域访问跨域资源
                            .setHeader("Access-Control-Allow-Origin", "*")
                            // 允许所有请求方式
                            .setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
                            // 有效时间
                            .setHeader("Access-Control-Max-Age", "3600")
                            // 允许的header参数
                            .setHeader("Access-Control-Allow-Headers", "*")

                            // 是否可以在iframe显示视图： DENY=不可以 | SAMEORIGIN=同域下可以 | ALLOW-FROM uri=指定域名下可以
                            .setHeader("X-Frame-Options", "SAMEORIGIN")
                            // 是否启用浏览器默认XSS防护： 0=禁用 | 1=启用 | 1; mode=block 启用, 并在检查到XSS攻击时，停止渲染页面
                            .setHeader("X-XSS-Protection", "1; mode=block")
                            // 禁用浏览器内容嗅探
                            .setHeader("X-Content-Type-Options", "nosniff");

                    // 如果是预检请求，则立即返回到前端
                    SaRouter.match(SaHttpMethod.OPTIONS).back();
                });
    }

    /**
     *Sa-Token 参数配置 此配置会覆盖 application.yml 中的配置
     */
    @Bean
    @Primary
    public SaTokenConfig getSaTokenConfigPrimary() {
        Jwt jwt = evaConfig.getJwt();
        SaTokenConfig config = new SaTokenConfig();
        config.setTokenName(CommonConstant.ACCESS_TOKEN_KEY);//token名称 (同时也是cookie名称)
        //token通过heard头传递过来的 则可以加 否则从cookie中获取到token之后会判断前缀 没有前缀则无效 但是satoken存在cookie中时 不存前缀！
//        config.setTokenPrefix(jwt.getTokenHead());
        config.setIsShare(false);
        config.setIsConcurrent(false);
//       过期策略
//        config.setTimeout(evaConfig.getJwt().getAlphaTtl());
        config.setTimeout(20);
        config.setAutoRenew(false);  //是否自动设置最后操作时间
        // token风格
        config.setTokenStyle("uuid");
        // jwt秘钥
        config.setJwtSecretKey(jwt.getSign());
        return config;
    }

    // Sa-Token 整合 jwt (Simple 简单模式)
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    /**
     * 不需要鉴权的静态资源配置
     */
    private String[] excludeStaticPaths(){
        String[] paths = null;
        var webstatic = evaConfig.getSecurity().getWebstatic();
        var staticPath = new String[]{
                "/",
                "/static/**",
                "/*.html",
                "/*.xls",
                "/*.xlsx",
                "/*.doc",
                "/*.docx",
                "/*.pdf",
                "/favicon.ico",
                "/*/*.html",
                "/*/*.css",
                "/*/*.js",
                "/*/swagger-resources/**",
                "/*/api-docs/**"
        };
        if (null != webstatic) {
            paths = ArrayUtil.addAll(webstatic, staticPath);
        }
        return paths;
    }

}
