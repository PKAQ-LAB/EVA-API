package io.nerv.security.jwt;

import lombok.Data;

@Data
public class CookieProperties {
    /** 可信任域 **/
    private String domain;

    /**cookie有效期**/
    private int maxAge = 60 * 60;
}
