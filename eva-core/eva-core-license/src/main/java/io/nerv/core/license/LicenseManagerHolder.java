package io.nerv.core.license;

import de.schlichtherle.license.*;
import io.nerv.properties.EvaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.prefs.Preferences;

/**
 * 证书管理器
 */
@Component
@ConditionalOnProperty(prefix = "eva.license", name = "enable", havingValue = "true")
public class LicenseManagerHolder {

    @Autowired
    private EvaConfig evaConfig;

    @Bean
    public LicenseManager getLicenseManager() {
        return new LicenseManager(this.initLicenseParams());
    }

    /**
     * 初始化证书的相关参数
     */
    private LicenseParam initLicenseParams() {
        Class<LicenseVerify> clazz = LicenseVerify.class;
        Preferences pre = Preferences.userNodeForPackage(clazz);
        CipherParam cipherParam = new DefaultCipherParam(evaConfig.getLicense().getKeystorePwd());
        KeyStoreParam pubStoreParam = new DefaultKeyStoreParam(clazz,
                evaConfig.getLicense().getPath(),
                evaConfig.getLicense().getAlias(),
                evaConfig.getLicense().getKeystorePwd() , null);
        return new DefaultLicenseParam(evaConfig.getLicense().getSubject(), pre, pubStoreParam, cipherParam);
    }

}