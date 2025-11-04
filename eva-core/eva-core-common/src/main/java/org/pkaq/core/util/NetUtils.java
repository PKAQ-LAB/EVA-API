package org.pkaq.core.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetUtils {

    /**
     * 获取指定 IP 对应的网卡 MAC 地址
     *
     * @param inetAddress IP 地址
     * @return MAC 地址字符串，例如 "00-1A-2B-3C-4D-5E"，无法获取返回 null
     */
    public static String getMacAddress(InetAddress inetAddress) {
        if (inetAddress == null) return null;
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(inetAddress);
            if (network == null) return null;

            byte[] macBytes = network.getHardwareAddress();
            if (macBytes == null || macBytes.length == 0) return null;

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < macBytes.length; i++) {
                sb.append(String.format("%02X%s", macBytes[i], (i < macBytes.length - 1) ? "-" : ""));
            }
            return sb.toString();
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        InetAddress local = InetAddress.getLocalHost();
        String mac = getMacAddress(local);
        System.out.println("MAC 地址: " + mac);
    }
}
