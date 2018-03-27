package org.pkaq.core.helper;

import java.util.Random;

/**
 * 订单帮助类
 * @author: S.PKAQ
 * @Datetime: 2018/3/26 23:52
 */
public class OrderHelper {
    private final static String prefix = "O";

    /**
     * 生成订单号
     * @param userCode
     * @return
     */
    public static String getOrderNumber(String userCode){
        long timestamp = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(timestamp);
        sb.append(userCode);
        sb.append(new Random().nextInt());
        return sb.toString();
    }
}
