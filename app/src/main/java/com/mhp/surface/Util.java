package com.mhp.surface;

import android.util.Log;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Util {

    /**
     * 获取ip地址
     *
     * @return
     */
    public static List<String> getHostIP() {

        List<String> hostIp = new ArrayList<String>();
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp.add(ip);
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("com.mhp.surface", "SocketException");
            e.printStackTrace();
        }
        return hostIp;
    }


    public static String getHostIPhtml() {

        String str = "";

        List<String> hostIps = getHostIP();

        for (String ip : hostIps) {

            str += ip + "</br>";

        }
        return str;
    }
}
