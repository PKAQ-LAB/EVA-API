package org.pkaq.web.core.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.pkaq.core.constant.CommonConstant;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.util.StrUtils;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 客户端IP和User-Agent信息解析器。
 *
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class ClientInfoResolver {

    private static final String UNKNOWN = "UNKNOWN";
    private static final Pattern ANDROID_MODEL = Pattern.compile("Android[^;]*;\\s*([^;)]+?)(?:\\s+Build/|;|\\))",
            Pattern.CASE_INSENSITIVE);

    private final EvaConfig evaConfig;

    /**
     * 解析客户端信息。
     *
     * @param request HTTP请求
     * @return 客户端信息
     */
    public ClientInfo resolve(HttpServletRequest request) {
        String userAgent = valueOrUnknown(request.getHeader("User-Agent"));
        String deviceType = resolveDeviceType(userAgent, request.getHeader(CommonConstant.DEVICE));
        String deviceModel = resolveDeviceModel(userAgent);
        NameVersion os = resolveOs(userAgent);
        NameVersion browser = resolveBrowser(userAgent);
        String clientVersion = valueOrUnknown(request.getHeader(CommonConstant.VERSION));
        String fingerprintSource = String.join("|", deviceType, deviceModel, os.name(), browser.name());
        return new ClientInfo(resolveIp(request), userAgent, deviceType, deviceModel, os.name(), os.version(),
                browser.name(), browser.version(), clientVersion, sha256(fingerprintSource));
    }

    private String resolveIp(HttpServletRequest request) {
        String remoteAddress = normalizeIp(request.getRemoteAddr());
        if (!isTrustedProxy(remoteAddress)) {
            return remoteAddress;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StrUtils.isNotBlank(forwardedFor)) {
            String[] addresses = forwardedFor.split(",");
            for (int index = addresses.length - 1; index >= 0; index--) {
                String candidate = normalizeIp(addresses[index]);
                if (candidate != null && !isTrustedProxy(candidate)) {
                    return candidate;
                }
            }
        }
        String realIp = normalizeIp(request.getHeader("X-Real-IP"));
        return realIp == null ? remoteAddress : realIp;
    }

    private boolean isTrustedProxy(String address) {
        if (address == null) {
            return false;
        }
        List<String> trustedProxies = evaConfig.getClientInfo().getTrustedProxies();
        return trustedProxies != null && trustedProxies.stream()
                .filter(StrUtils::isNotBlank)
                .anyMatch(range -> contains(range.trim(), address));
    }

    private boolean contains(String range, String address) {
        try {
            if (!range.contains("/")) {
                return InetAddress.getByName(range).equals(InetAddress.getByName(address));
            }
            String[] parts = range.split("/", 2);
            byte[] network = InetAddress.getByName(parts[0]).getAddress();
            byte[] candidate = InetAddress.getByName(address).getAddress();
            int prefixLength = Integer.parseInt(parts[1]);
            if (network.length != candidate.length || prefixLength < 0 || prefixLength > network.length * 8) {
                return false;
            }
            for (int bit = 0; bit < prefixLength; bit++) {
                int mask = 1 << (7 - bit % 8);
                if ((network[bit / 8] & mask) != (candidate[bit / 8] & mask)) {
                    return false;
                }
            }
            return true;
        } catch (UnknownHostException | NumberFormatException exception) {
            return false;
        }
    }

    private String normalizeIp(String value) {
        if (StrUtils.isBlank(value)) {
            return null;
        }
        String candidate = value.trim();
        try {
            if (candidate.contains(":")) {
                return candidate.matches("[0-9a-fA-F:]+")
                        ? InetAddress.getByName(candidate).getHostAddress() : null;
            }
            String[] segments = candidate.split("\\.", -1);
            if (segments.length != 4) {
                return null;
            }
            StringBuilder normalized = new StringBuilder();
            for (String segment : segments) {
                if (!segment.matches("[0-9]{1,3}")) {
                    return null;
                }
                int octet = Integer.parseInt(segment);
                if (octet > 255) {
                    return null;
                }
                if (!normalized.isEmpty()) {
                    normalized.append('.');
                }
                normalized.append(octet);
            }
            return normalized.toString();
        } catch (UnknownHostException exception) {
            return null;
        }
    }

    private String resolveDeviceType(String userAgent, String declaredDevice) {
        String lowerAgent = userAgent.toLowerCase(Locale.ROOT);
        if (lowerAgent.contains("bot") || lowerAgent.contains("spider") || lowerAgent.contains("crawler")) {
            return "BOT";
        }
        if (lowerAgent.contains("ipad") || lowerAgent.contains("tablet")) {
            return "TABLET";
        }
        if (lowerAgent.contains("mobile") || lowerAgent.contains("iphone") || lowerAgent.contains("android")) {
            return "MOBILE";
        }
        if (!UNKNOWN.equals(userAgent)) {
            return "DESKTOP";
        }
        return valueOrUnknown(declaredDevice).toUpperCase(Locale.ROOT);
    }

    private String resolveDeviceModel(String userAgent) {
        Matcher matcher = ANDROID_MODEL.matcher(userAgent);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        if (userAgent.contains("iPad")) {
            return "iPad";
        }
        if (userAgent.contains("iPhone")) {
            return "iPhone";
        }
        return UNKNOWN;
    }

    private NameVersion resolveOs(String userAgent) {
        if (userAgent.contains("Windows NT 10.0")) {
            return new NameVersion("Windows", "10+");
        }
        NameVersion android = match(userAgent, "Android", "Android[ /]([0-9.]+)");
        if (!UNKNOWN.equals(android.name())) {
            return android;
        }
        NameVersion ios = match(userAgent, "iOS", "(?:CPU (?:iPhone )?OS|iPhone OS) ([0-9_]+)");
        if (!UNKNOWN.equals(ios.name())) {
            return new NameVersion(ios.name(), ios.version().replace('_', '.'));
        }
        NameVersion macOs = match(userAgent, "macOS", "Mac OS X ([0-9_]+)");
        if (!UNKNOWN.equals(macOs.name())) {
            return new NameVersion(macOs.name(), macOs.version().replace('_', '.'));
        }
        if (userAgent.contains("Linux")) {
            return new NameVersion("Linux", UNKNOWN);
        }
        return NameVersion.unknown();
    }

    private NameVersion resolveBrowser(String userAgent) {
        NameVersion edge = match(userAgent, "Edge", "Edg(?:A|iOS)?/([0-9.]+)");
        if (!UNKNOWN.equals(edge.name())) {
            return edge;
        }
        NameVersion chrome = match(userAgent, "Chrome", "(?:Chrome|CriOS)/([0-9.]+)");
        if (!UNKNOWN.equals(chrome.name())) {
            return chrome;
        }
        NameVersion firefox = match(userAgent, "Firefox", "(?:Firefox|FxiOS)/([0-9.]+)");
        if (!UNKNOWN.equals(firefox.name())) {
            return firefox;
        }
        if (userAgent.contains("Safari/") && userAgent.contains("Version/")) {
            return match(userAgent, "Safari", "Version/([0-9.]+)");
        }
        return NameVersion.unknown();
    }

    private NameVersion match(String value, String name, String expression) {
        Matcher matcher = Pattern.compile(expression, Pattern.CASE_INSENSITIVE).matcher(value);
        return matcher.find() ? new NameVersion(name, matcher.group(1)) : NameVersion.unknown();
    }

    private String valueOrUnknown(String value) {
        return StrUtils.isBlank(value) ? UNKNOWN : value.trim();
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前JVM不支持SHA-256", exception);
        }
    }

    private record NameVersion(String name, String version) {
        private static NameVersion unknown() {
            return new NameVersion(UNKNOWN, UNKNOWN);
        }
    }
}
