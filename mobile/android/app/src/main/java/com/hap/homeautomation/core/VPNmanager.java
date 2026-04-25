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

public class VPNmanager {

    private static final String ZT_PACKAGE = "com.zerotier.one";

    // Method to check ZeroTier One installation
    public static boolean checkInstallation(Context context) {
        try {
            context.getPackageManager().getPackageInfo(ZT_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // Method to open the Play Store for ZeroTier One installation
    public static void openPlayStore(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + ZT_PACKAGE)));
        } catch (Exception e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + ZT_PACKAGE)));
        }
    }

    // Method to open ZeroTier One
    public static void openZeroTier(Context context) {

        try {
            Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage(ZT_PACKAGE);

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception ignored) {}
    }

    // Method to check VPN connection
    public static boolean checkVPNconnection(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
    }

    // Method to check Server connection
    public static boolean checkserverConnection(String host, int port, int timeoutMs) {

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;

        } catch (IOException e) {
            return false;
        }
    }
}
