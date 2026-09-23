package org.pkaq.core.properties;

import lombok.Data;

/**
 * @author PKAQ
 */
@Data
public class Cookie {
    /**
     * 可信任域
     **/
    private String domain;

    /**
     * cookie有效期
     **/
    private int maxAge = 60 * 60;

    /**
     * 是否仅允许 HTTPS 传输。
     */
    private boolean secure;

    /**
     * Cookie 跨站策略。
     */
    private String sameSite = "Lax";
}
