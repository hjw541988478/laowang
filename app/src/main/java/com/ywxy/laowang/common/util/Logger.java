package com.ywxy.laowang.common.util;

import android.util.Log;

import com.ywxy.laowang.BuildConfig;

/**
 * Created by hjw on 2015/9/1 0001.
 */
public class Logger {

    private static boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "Logger";

    public static void d(String msg) {
        if (DEBUG)
            Log.d(TAG, msg);
    }

    public static void e(String msg) {
        if (DEBUG)
            Log.e(TAG, msg);
    }
}
