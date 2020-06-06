package io.nerv.core.mvc.ctrl

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import springfox.documentation.annotations.ApiIgnore
import java.io.IOException
import javax.servlet.http.HttpServletResponse

/**
 * 默认控制器 访问swagger首页
 * @author: S.PKAQ
 * @Datetime: 2018/9/29 10:53
 */
@Controller
class IndexCtrl {
    @RequestMapping(["/"])
    @ApiIgnore
    @Throws(IOException::class)
    fun index(response: HttpServletResponse) {
        response.sendRedirect("doc.html")
    }
}