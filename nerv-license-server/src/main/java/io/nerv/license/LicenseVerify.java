package io.nerv.license;

import cn.hutool.core.net.NetUtil;
import de.schlichtherle.license.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.InetAddress;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;

@Slf4j
@Component
public class LicenseVerify {

    @Autowired
    private LicenseConfig licenseConfig;

    /**
     * 安装证书证书
     */
    public void install() {
        try {
            LicenseManager licenseManager = getLicenseManager();
            licenseManager.install(new File(licenseConfig.getLicPath()));
            log.info("安装证书成功!");
        } catch (Exception e) {
            log.error("授权已过期, 安装证书失败!", e);
            Runtime.getRuntime().halt(1);
        }

    }

    private LicenseManager getLicenseManager() {
        return LicenseManagerHolder.getLicenseManager(initLicenseParams());
    }

    /**
     * 初始化证书的相关参数
     */
    private LicenseParam initLicenseParams() {
        Class<LicenseVerify> clazz = LicenseVerify.class;
        Preferences pre = Preferences.userNodeForPackage(clazz);
        CipherParam cipherParam = new DefaultCipherParam(licenseConfig.getPub().getKeystorePwd());
        KeyStoreParam pubStoreParam = new DefaultKeyStoreParam(clazz,
                                                               licenseConfig.getPub().getPath(),
                                                               licenseConfig.getPub().getAlias(),
                                                               licenseConfig.getPub().getKeystorePwd() , null);
        return new DefaultLicenseParam(licenseConfig.getSubject(), pre, pubStoreParam, cipherParam);
    }

    /**
     * 验证证书的合法性
     */
    public boolean vertify() {
        try {
            LicenseManager licenseManager = getLicenseManager();
            LicenseContent verify = licenseManager.verify();
            log.info("验证证书成功!");
            Map<String, String> extra = (Map) verify.getExtra();
            String ip = extra.get("ip");
            InetAddress inetAddress = InetAddress.getLocalHost();
            String localIp = inetAddress.toString().split("/")[1];
            if (!Objects.equals(ip, localIp)) {
                log.error("IP 地址验证不通过");
                return false;
            }
            String mac = extra.get("mac");
            String localMac = getLocalMac(inetAddress);
            if (!Objects.equals(mac, localMac)) {
                log.error("MAC 地址验证不通过");
                return false;
            }
            log.info("IP、MAC地址验证通过");
            return true;
        } catch (LicenseContentException ex) {
            log.error("证书已经过期!", ex);
            return false;
        } catch (Exception e) {
            log.error("验证证书失败!", e);
            return false;
        }
    }

    /**
     * 得到本机 mac 地址
     *
     * @param inetAddress
     */
    private String getLocalMac(InetAddress inetAddress) {
        return NetUtil.getMacAddress(inetAddress).toUpperCase();
    }
}