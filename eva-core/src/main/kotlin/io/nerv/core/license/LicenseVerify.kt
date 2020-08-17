package io.nerv.core.license

import cn.hutool.core.io.resource.ClassPathResource
import cn.hutool.core.net.NetUtil
import de.schlichtherle.license.LicenseContentException
import de.schlichtherle.license.LicenseManager
import io.nerv.properties.EvaConfig
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.io.File
import java.net.InetAddress

@Component
@ConditionalOnProperty(prefix = "eva.license", name = ["enable"], havingValue = "true")
class LicenseVerify {

    val log = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private val evaConfig: EvaConfig? = null

    @Autowired
    private val licenseManager: LicenseManager? = null

    /**
     * 初始化安装证书证书
     */
    fun init() {
        try {
            println(File(evaConfig!!.license!!.license).absolutePath)
            val classPathResource = ClassPathResource(evaConfig.license!!.license)
            licenseManager!!.install(classPathResource.file)
            log.info("安装证书成功!")
        } catch (e: Exception) {
            log.error("授权已过期, 安装证书失败!", e)
            Runtime.getRuntime().halt(1)
        }
    }

    /**
     * 安装证书证书
     */
    fun install() {
        try {
            licenseManager!!.install(File(evaConfig!!.license!!.license))
            log.info("安装证书成功!")
        } catch (e: Exception) {
            log.error("授权已过期, 安装证书失败!", e)
            Runtime.getRuntime().halt(1)
        }
    }

    /**
     * 验证证书的合法性
     */
    fun vertify(): Boolean {
        return try {
            val verify = licenseManager!!.verify()
            log.info("验证证书成功!")
            val extra: Map<*, *> = verify.extra as Map<*, *>

            val ip = extra["ip"]
            val inetAddress = InetAddress.getLocalHost()
            val localIp = inetAddress.toString().split("/".toRegex()).toTypedArray()[1]
            if (ip != localIp) {
                log.error("IP 地址验证不通过")
                return false
            }
            val mac = extra["mac"]
            val localMac = getLocalMac(inetAddress)
            if (mac != localMac) {
                log.error("MAC 地址验证不通过")
                return false
            }
            log.info("IP、MAC地址验证通过")
            true
        } catch (ex: LicenseContentException) {
            log.error("证书已经过期!", ex)
            false
        } catch (e: Exception) {
            log.error("验证证书失败!", e)
            false
        }
    }

    /**
     * 得到本机 mac 地址
     *
     * @param inetAddress
     */
    private fun getLocalMac(inetAddress: InetAddress): String {
        return NetUtil.getMacAddress(inetAddress).toUpperCase()
    }
}