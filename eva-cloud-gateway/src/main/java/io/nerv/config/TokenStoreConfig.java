package io.nerv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.security.KeyPair;


@Configuration
public class TokenStoreConfig {
	private final String jks = "C:\\Users\\PKAQ\\jwtpub.jks";

	private final String pwd = "7u8i9o0p";

	private final String alias = "evapub";

	//使用jwt替换原有的uuid生成token方式
	@Bean
	public JwtAccessTokenConverter jwtAccessTokenConverter() {
		JwtAccessTokenConverter accessTokenConverter = new JwtAccessTokenConverter();
		//配置JWT使用的秘钥
		accessTokenConverter.setKeyPair(keyPair());
		return accessTokenConverter;
	}

	@Bean
	public KeyPair keyPair() {
		//从classpath下的证书中获取秘钥对
		KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new FileSystemResource(jks), pwd.toCharArray());
		return keyStoreKeyFactory.getKeyPair(alias, pwd.toCharArray());
	}
}