package io.nerv.manager;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.stereotype.Component;

@Component
public class ResJwtTokenStore extends JwtTokenStore {
	public ResJwtTokenStore(JwtAccessTokenConverter jwtTokenEnhancer) {
		super(jwtTokenEnhancer);
	}

	@Override
	public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {

		Object object = authentication.getPrincipal() ;
		
		token.getAdditionalInformation().put("user", object ) ;
		
	}
	 
}