package com.hap.homeautomation.core;

import java.net.InetSocketAddress;
import java.net.Socket;
import android.net.Uri;

public class DevicesConnectivityChecker {

    // Method to verify if the device is ONLINE
    public static boolean isOnline(String ip, int port, int timeoutMs) {

        try (Socket socket = new Socket()) {
            socket.connect(
                    new InetSocketAddress(ip, port),
                    timeoutMs
            );
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    // Method to get the device IP
    public static String getIp(String url) {

        try {
            Uri uri = Uri.parse(url);
            return uri.getHost();

        } catch (Exception e) {
            return "";
        }
    }

    // Method to get the device port
    public static int getPort(String url) {

        try {
            Uri uri = Uri.parse(url);
            int port = uri.getPort();
            return port != -1 ? port : 554;

        } catch (Exception e) {
            return 554;
        }
    }
}