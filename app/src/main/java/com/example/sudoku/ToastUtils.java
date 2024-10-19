package com.example.sudoku;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class ToastUtils {

    public static void showToast(Context context, String message, int durationInMillis) {
        // Create the Toast message
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);

        // Show the toast
        toast.show();

        // Use a Handler to cancel the toast after the specified duration
        new Handler().postDelayed(toast::cancel, durationInMillis);
    }
}
