package io.nerv.core.license

import io.nerv.core.enums.BizCodeEnum
import io.nerv.core.mvc.util.Response
import io.nerv.core.util.JsonUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * license 验证拦截器
 */
@Component
@ConditionalOnProperty(prefix = "eva.license", name = ["enable"], havingValue = "true")
class LicenseCheckInterceptor : HandlerInterceptorAdapter() {
    @Autowired
    private val jsonUtil: JsonUtil? = null

    @Autowired
    private val licenseVerify: LicenseVerify? = null

    @Throws(Exception::class)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val licenseVerify = LicenseVerify()

        //校验证书是否有效
        val verifyResult = licenseVerify.vertify()
        return if (verifyResult) {
            true
        } else {
            response.characterEncoding = "UTF-8"
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.status = HttpServletResponse.SC_OK
            response.writer.use { printWriter ->
                printWriter.write(jsonUtil!!.toJSONString(
                        Response()
                                .failure(BizCodeEnum.LICENSE_LICENSEHASEXPIRED)))
                printWriter.flush()
            }
            false
        }
    }
}