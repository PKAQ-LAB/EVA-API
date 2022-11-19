package io.nerv.core.properties;

import lombok.Data;

@Data
public class Cookie {
    /** 可信任域 **/
    private String domain;

    /**cookie有效期**/
    private int maxAge = 60 * 60;
}
