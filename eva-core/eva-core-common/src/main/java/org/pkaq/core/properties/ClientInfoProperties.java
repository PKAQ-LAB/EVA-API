package org.pkaq.core.properties;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端信息采集配置。
 *
 * @author PKAQ
 */
@Data
public class ClientInfoProperties {

    /**
     * 允许提供转发IP请求头的可信代理IP或CIDR。
     */
    private List<String> trustedProxies = new ArrayList<>();

    /**
     * 在线会话最后活动时间的最小写入间隔，单位秒。
     */
    private long activityUpdateIntervalSeconds = 60L;
}
