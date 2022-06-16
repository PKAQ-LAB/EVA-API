package io.nerv.license;

import cn.hutool.core.date.DateUtil;
import de.schlichtherle.license.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.security.auth.x500.X500Principal;
import java.io.File;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

@Slf4j
@Component
public class LicenseGenerator {

    @Autowired
    private LicenseConfig licenseConfig;
    /**
     * X500Princal 是一个证书文件的固有格式，详见API
     */
    private final static X500Principal DEFAULT_HOLDERAND_ISSUER = new X500Principal("CN=Eva, OU=Nerv, O=Nerv, L=Nerv, ST=Unknown, C=CN");

    /**
     * 生成证书，在证书发布者端执行
     *
     * @throws Exception
     */
    public void create() throws Exception {
        LicenseManager licenseManager = LicenseManagerHolder.getLicenseManager(initLicenseParams());
        licenseManager.store(buildLicenseContent(), new File(licenseConfig.getLicPath()));
        log.info("------ 证书发布成功 ------");
    }

    /**
     * 初始化证书的相关参数
     *
     * @return
     */
    private LicenseParam initLicenseParams() {
        Class<LicenseGenerator> clazz = LicenseGenerator.class;
        Preferences preferences = Preferences.userNodeForPackage(clazz);
        // 设置对证书内容加密的对称密码
        CipherParam cipherParam = new DefaultCipherParam(licenseConfig.getPriv().getKeystorePwd());
        // 参数 1,2 从哪个Class.getResource()获得密钥库;
        // 参数 3 密钥库的别名;
        // 参数 4 密钥库存储密码;
        // 参数 5 密钥库密码
        KeyStoreParam privateStoreParam = new DefaultKeyStoreParam(clazz, licenseConfig.getPriv().getPath(),
                                                                          licenseConfig.getPriv().getKeyAlias(),
                                                                          licenseConfig.getPriv().getKeystorePwd(),
                                                                          licenseConfig.getPriv().getKeyPwd());
        // 返回生成证书时需要的参数
        return new DefaultLicenseParam(licenseConfig.getSubject(), preferences, privateStoreParam, cipherParam);
    }

    /**
     * 通过外部配置文件构建证书的的相关信息
     *
     * @return
     * @throws ParseException
     */
    public LicenseContent buildLicenseContent() throws ParseException {
        LicenseContent content = new LicenseContent();
//        SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        content.setConsumerAmount(licenseConfig.getConsumerAmount());
        content.setConsumerType(licenseConfig.getConsumerType());
        content.setHolder(DEFAULT_HOLDERAND_ISSUER);
        content.setIssuer(DEFAULT_HOLDERAND_ISSUER);
        content.setIssued( DateUtil.parseDateTime(licenseConfig.getIssuedTime()) );
        content.setNotBefore( DateUtil.parseDateTime(licenseConfig.getNotBefore()) );
        content.setNotAfter( DateUtil.parseDateTime(licenseConfig.getNotAfter()) );
        content.setInfo(licenseConfig.getInfo());
        // 扩展字段
        Map<String, String> map = new HashMap<>(4);
        map.put("ip", licenseConfig.getIpAddress());
        map.put("mac", licenseConfig.getMacAddress());
        content.setExtra(map);

        return content;
    }
}