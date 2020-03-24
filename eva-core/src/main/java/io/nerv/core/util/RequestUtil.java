package io.nerv.core.util;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.constant.CommonConstant;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 请求处理工具类
 */
@Component
public class RequestUtil {
    /**
     * 获取请求设备类型
     * @param request
     * @return
     */
    public String getDeivce(HttpServletRequest request){
        String device = request.getHeader(CommonConstant.DEVICE);

        return StrUtil.isBlank(device)? CommonConstant.UNKNOWN : device;
    }

    /**
     * 获取请求版本号
     * @param request
     * @return
     */
    public String getVersion(HttpServletRequest request){
        String version = request.getHeader(CommonConstant.VERSION);

        return StrUtil.isBlank(version)? CommonConstant.UNKNOWN : version;
    }

    /**
     * 根据给定的字符串格式化返回设备类型和版本号
     * @param request
     * @param format
     * @return
     */
    public String formatDeivceAndVersion(HttpServletRequest request, String format){
        return String.format(format, this.getDeivce(request), this.getVersion(request));
    }
}
