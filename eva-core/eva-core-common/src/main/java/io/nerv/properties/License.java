package io.nerv.properties;

import lombok.Data;

/**
 * license 配置
 *
 * @author: S.PKAQ
 */
@Data
public class License {
    // 是否启用认证授权
    private boolean enable;
    // 公钥别名
    private String alias;
    // 该密码是访问密钥库的密码 — 使用 keytool 生成密钥对时设置，使用者知道该密码
    private String keystorePwd;
    // 公共库路径（放在 resource 目录下）
    private String path;
    // 证书路径
    private String license;
    // 项目名
    private String subject;

}
