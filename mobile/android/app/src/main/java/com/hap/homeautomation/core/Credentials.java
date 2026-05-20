package com.hap.homeautomation.core;

public class Credentials {

    private static final String LOCAL_IP = "192.168.68.11";
    private static final String VPN_IP = "10.147.19.177";

    public static final int SERVER_PORT = 1883;

    private static ConnectionMode currentMode = ConnectionMode.LOCAL;

    public static void setConnectionMode(ConnectionMode mode) {
        currentMode = mode;
    }

    public static ConnectionMode getConnectionMode() {
        return currentMode;
    }

    public static String getServerIP() {

        switch (currentMode) {

            case LOCAL:
                return LOCAL_IP;

            case VPN:
                return VPN_IP;

            default:
                return VPN_IP;
        }
    }

    public static String getFirmwareURL() {
        return "http://" + getServerIP() + "/firmware/firmware.json";
    }
}