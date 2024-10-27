package com.example.sudoku;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

    // Method to check if network is available
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get the active network info
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        // Return true if there is an active network and it's connected
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
