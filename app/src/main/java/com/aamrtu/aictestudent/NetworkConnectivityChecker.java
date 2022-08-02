package com.aamrtu.aictestudent;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkConnectivityChecker {
    static boolean isNetworkConnected(Context context) {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            final Network net = connectivityManager.getActiveNetwork();
            if (net != null) {
                final NetworkCapabilities netCapability = connectivityManager.getNetworkCapabilities(net);
                return (netCapability.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
            }
        }
        return false;
    }
}
