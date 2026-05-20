package com.hap.homeautomation.core;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConnectionManager {

    // ZeroTier package
    private static final String ZT_PACKAGE = "com.zerotier.one";

    // Method to make a LOCAL connection
    public static boolean connectLocal() {

        Credentials.setConnectionMode(ConnectionMode.LOCAL);

        return checkLocalConnection(
                Credentials.getServerIP(),
                Credentials.SERVER_PORT,
                1500);
    }

    // Method to make a VPN connection
    public static boolean connectVPN(Context context) {

        if (!isVPNinstalled(context)) {
            return false;
        }

        Credentials.setConnectionMode(ConnectionMode.VPN);

        return checkLocalConnection(
                Credentials.getServerIP(), Credentials.SERVER_PORT, 2000);
    }

    // Method to connect to the server
    public static ConnectionMode establishConnection(Context context) {

        if (connectLocal()) {
            return ConnectionMode.LOCAL;
        }

        if (connectVPN(context)) {
            return ConnectionMode.VPN;
        }

        Credentials.setConnectionMode(ConnectionMode.NONE);

        return ConnectionMode.NONE;
    }

    // Method to check connection status
    public static boolean isConnected(Context context) {

        return establishConnection(context) != ConnectionMode.NONE;
    }

    // Method to check LOCAL connection
    public static boolean checkLocalConnection(String host, int port, int timeoutMs) {

        try (Socket socket = new Socket()) {

            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;

        } catch (IOException e) {
            return false;
        }
    }

    // Method to check VPN installation
    public static boolean isVPNinstalled(Context context) {

        try {
            context.getPackageManager().getPackageInfo(ZT_PACKAGE, 0);
            return true;

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // Method to open Play Store
    public static void openPlayStore(Context context) {

        try {
            context.startActivity(new Intent(
                    Intent.ACTION_VIEW, Uri.parse("market://details?id=" + ZT_PACKAGE)));

        } catch (Exception e) {

            context.startActivity(new Intent(
                            Intent.ACTION_VIEW, Uri.parse(
                                    "https://play.google.com/store/apps/details?id="
                                            + ZT_PACKAGE)));
        }
    }

    // Method to open the VPN app
    public static void openVPNapp(Context context) {

        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(ZT_PACKAGE);

            if (intent != null) {

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

        } catch (Exception ignored) {}
    }

    // Method to check VPN connection
    public static boolean isVPNconnected(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities caps = cm.getNetworkCapabilities(network);

        return caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
    }
}