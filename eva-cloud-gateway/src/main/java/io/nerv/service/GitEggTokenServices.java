//package io.nerv.service;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class RefreshTokenServices extends DefaultTokenServices {
//
//    private final RedisTemplate redisTemplate;
//
//    public GitEggTokenServices(RedisTemplate redisTemplate)
//    {
//        this.redisTemplate = redisTemplate;
//    }
//
//    @Transactional(
//            noRollbackFor = {InvalidTokenException.class, InvalidGrantException.class}
//    )
//    @Override
//    public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, TokenRequest tokenRequest) throws AuthenticationException {
//
//        JSONObject jsonObject = null;
//        String jti = null;
//        //如果refreshToken被加入到黑名单，就是执行了退出登录操作，那么拒绝访问
//        try {
//            JWSObject jwsObject = JWSObject.parse(refreshTokenValue);
//            Payload payload = jwsObject.getPayload();
//            jsonObject = payload.toJSONObject();
//            jti = jsonObject.getAsString(TokenConstant.JTI);
//            String blackListToken = (String)redisTemplate.opsForValue().get(AuthConstant.TOKEN_BLACKLIST + jti);
//            if (!StringUtils.isEmpty(blackListToken)) {
//                throw new InvalidTokenException("Invalid refresh token (blackList): " + refreshTokenValue);
//            }
//        } catch (ParseException e) {
//            log.error("获取refreshToken黑名单时发生错误：{}", e);
//        }
//
//       OAuth2AccessToken oAuth2AccessToken = super.refreshAccessToken(refreshTokenValue, tokenRequest);
//
//        // RefreshToken不支持重复使用，如果使用一次，则加入黑名单不再允许使用，当刷新token执行完之后，即校验过RefreshToken之后，才执行存redis操作
//        if (null != jsonObject && !StringUtils.isEmpty(jti)) {
//            long currentTimeSeconds = System.currentTimeMillis() / GitEggConstant.Number.THOUSAND;
//            Long exp = Long.parseLong(jsonObject.getAsString(TokenConstant.EXP));
//            if (exp - currentTimeSeconds > GitEggConstant.Number.ZERO) {
//                redisTemplate.opsForValue().set(AuthConstant.TOKEN_BLACKLIST + jti, jti, (exp - currentTimeSeconds), TimeUnit.SECONDS);
//            }
//        }
//
//        return oAuth2AccessToken;
//    }
//}