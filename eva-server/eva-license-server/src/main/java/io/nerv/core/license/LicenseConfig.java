package io.nerv.core.license;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "eva.license")
@EnableConfigurationProperties(LicenseConfig.class)
public class LicenseConfig {
    // 项目的唯一识别码
    private String subject;
    // 发布日期
    private String issuedTime;
    // 有效开始日期
    private String notBefore;
    // 有效截止日期
    private String notAfter;
    // ip 地址
    private String ipAddress;
    // mac 地址
    private String macAddress;
    // 使用者类型，用户(user)、电脑(computer)、其他（else）
    private String consumerType;
    // 证书允许使用的消费者数量
    private int consumerAmount;
    // 证书说明
    private String info;
    //生成证书的地址
    private String licPath;

    private Priv priv;

    private Pub pub;

    // 私钥配置
    @Data
    public static class Priv {
        // 私钥的别名
        private String keyAlias;
        // privateKeyPwd（该密码是生成密钥对的密码 — 需要妥善保管，不能让使用者知道）
        private String keyPwd;
        // keyStorePwd（该密码是访问密钥库的密码 — 使用 keytool 生成密钥对时设置，使用者知道该密码）
        private String keystorePwd;
        // 密钥库的地址（放在 resource 目录下）
        private String path;
    }

    // 公钥配置
    @Data
    public static class Pub {
        // 公钥别名
        private String alias;
        // 该密码是访问密钥库的密码 — 使用 keytool 生成密钥对时设置，使用者知道该密码
        private String keystorePwd;
        // 公共库路径（放在 resource 目录下
        private String path;
        // 证书路径（我这边配置在了 linux 根路径下，即 /license.lic ）
        private String license;
    }

}
