package org.pkaq.core.util;

import org.springframework.util.DigestUtils;

public class SecureUtils {
    public static String md5(String data) {
        return DigestUtils.md5DigestAsHex(data.getBytes());
    }
}
