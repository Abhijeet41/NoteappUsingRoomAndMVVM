package com.abhi41.noteapproomdb.UI.util;

import android.app.Application;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Timer;
import java.util.TimerTask;

public class MyApplication extends Application {

    private static LogoutListener logoutListener = null;
    private static Timer timer = null;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static void userSessionStart() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (logoutListener != null) {
                    logoutListener.onSessionLogout();
                    Log.d("App", "Session Destroyed");
                }
            }
        },  1000 * 60  );//1000 * 60 * 2
    }

    public static void resetSession() {
        userSessionStart();
    }

    public static void registerSessionListener(LogoutListener listener) {
        logoutListener = listener;
    }

    public interface LogoutListener{
        void onSessionLogout();
    }

}
