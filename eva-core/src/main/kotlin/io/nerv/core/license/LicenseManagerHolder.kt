package io.nerv.core.license

import de.schlichtherle.license.*
import io.nerv.properties.EvaConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.prefs.Preferences

/**
 * 证书管理器
 */
@Component
@ConditionalOnProperty(prefix = "eva.license", name = ["enable"], havingValue = "true")
class LicenseManagerHolder {
    @Autowired
    private val evaConfig: EvaConfig? = null

    @get:Bean
    val licenseManager: LicenseManager
        get() = LicenseManager(initLicenseParams())

    /**
     * 初始化证书的相关参数
     */
    private fun initLicenseParams(): LicenseParam {
        val clazz = LicenseVerify::class.java
        val pre = Preferences.userNodeForPackage(clazz)
        val cipherParam: CipherParam = DefaultCipherParam(evaConfig!!.license!!.keystorePwd)
        val pubStoreParam: KeyStoreParam = DefaultKeyStoreParam(clazz,
                evaConfig.license!!.path,
                evaConfig.license!!.alias,
                evaConfig.license!!.keystorePwd, null)
        return DefaultLicenseParam(evaConfig.license!!.subject, pre, pubStoreParam, cipherParam)
    }
}