package io.nerv.jwt;

import io.nerv.core.security.domain.JwtUserDetail;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;

/**
 * Jwt内容增强器
 */
public class JwtTokenEnhancer implements TokenEnhancer {
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        JwtUserDetail jwtUserDetail = (JwtUserDetail) authentication.getPrincipal();

        Map<String, Object> info = new HashMap<>();
        info.put("id", jwtUserDetail.getId());
        info.put("account", jwtUserDetail.getAccount());
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);

        return accessToken;
    }
}