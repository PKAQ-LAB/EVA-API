package io.nerv.weixin.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


/**
 * 企业微信配置类
 */
@Data
@ConfigurationProperties(prefix = "wechat.cp")
public class WxCpProperties {
  /**
   * 设置微信企业号的corpId
   */
  private String corpId;

  /**
   * 通讯录密钥
   */
  private String contactSecret;

  private List<AppConfig> appConfigs;

  @Data
  public static class AppConfig {
    /**
     * 设置微信企业应用的AgentId
     */
    private Integer agentId;

    /**
     * 设置微信企业应用的Secret
     */
    private String secret;

    /**
     * 设置微信企业号的token
     */
    private String token;

    /**
     * 设置微信企业号的EncodingAESKey
     */
    private String aesKey;

  }
}