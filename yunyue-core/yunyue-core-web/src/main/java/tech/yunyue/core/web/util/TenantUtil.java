package tech.yunyue.core.web.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.yunyue.core.properties.EvaConfig;



@Component
@RequiredArgsConstructor
public class TenantUtil {
    private final EvaConfig evaConfig;

    /**
     * 获取Tenant Id
     *
     * @param request
     * @return
     */
    public String getTenantId(String uid) {
        //从redis中获取

        //redis没有就从数据库读取


        return "";
    }
}
