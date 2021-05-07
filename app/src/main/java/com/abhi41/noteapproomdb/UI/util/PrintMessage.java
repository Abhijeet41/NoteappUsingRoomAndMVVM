package com.abhi41.noteapproomdb.UI.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class PrintMessage {

    public static void ToastMessage(Context context,String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void LogD(String TAG,String message)
    {
        Log.d(TAG, message);
    }
}
