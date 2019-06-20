package io.nerv.core.util;

import java.util.UUID;

/**
 * UUID生成器
 * @author: S.PKAQ
 * @Datetime: 2018/9/28 13:42
 */
public class UUIDHelper {
    public static String id(){
        return UUID.randomUUID().toString().replace("-","");
    }
}
