package org.pkaq.druid;

import com.alibaba.druid.filter.config.ConfigTools;

/**
 * Created with IntelliJ IDEA.
 * Author: S.PKAQ
 * Datetime: 2018/3/5 14:36
 */
public class DruidTest {
    public static void main(String[] args) throws Exception {
        String password = "jxc";
        String[] arr = ConfigTools.genKeyPair(512);
        System.out.println("privateKey:" + arr[0]);
        System.out.println("publicKey:" + arr[1]);
        System.out.println("password:" + ConfigTools.encrypt(arr[0], password));
    }
}
