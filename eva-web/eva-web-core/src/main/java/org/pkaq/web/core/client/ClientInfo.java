package org.pkaq.web.core.client;

/**
 * 从HTTP请求中提取的客户端信息。
 *
 * @param ip 客户端IP
 * @param userAgent User-Agent
 * @param deviceType 设备类型
 * @param deviceModel 设备型号
 * @param osName 操作系统名称
 * @param osVersion 操作系统版本
 * @param browserName 浏览器名称
 * @param browserVersion 浏览器版本
 * @param clientVersion 客户端主动上报版本
 * @param fingerprint 设备特征摘要
 * @author PKAQ
 */
public record ClientInfo(String ip,
                         String userAgent,
                         String deviceType,
                         String deviceModel,
                         String osName,
                         String osVersion,
                         String browserName,
                         String browserVersion,
                         String clientVersion,
                         String fingerprint) {
}
