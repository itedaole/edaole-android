/**
 * 
 */

package com.john.util;

import android.util.Log;

/**
 * 日志输出工具集
 * 
 * @author <a href="mailto:chen.jackson@gmail.com">Mike Chen</a>
 * @version 1.0
 */
public class LogUtil {
    private static final String TAG = "groupbuy";

    private static final boolean IS_DISPLAY_LOG = false;

    public static void info(String msg) {
        if (IS_DISPLAY_LOG) {
            Log.i(TAG, msg);
        }
    }

    public static void info(String msg, Throwable tr) {
        if (IS_DISPLAY_LOG) {
            Log.i(TAG, msg, tr);
        }
    }

    public static void debug(String msg) {
        if (IS_DISPLAY_LOG) {
            Log.d(TAG, msg);
        }
    }

    public static void debug(String msg, Throwable tr) {
        if (IS_DISPLAY_LOG) {
            Log.d(TAG, msg, tr);
        }
    }

    public static void verbose(String msg) {
        if (IS_DISPLAY_LOG) {
            Log.v(TAG, msg);
        }
    }

    public static void verbose(String msg, Throwable tr) {
        if (IS_DISPLAY_LOG) {
            Log.v(TAG, msg, tr);
        }
    }

    public static void warn(String msg) {
        if (IS_DISPLAY_LOG) {
            Log.w(TAG, msg);
        }
    }

    public static void warn(String msg, Throwable tr) {
        if (IS_DISPLAY_LOG) {
            Log.w(TAG, msg, tr);
        }
    }

    public static void error(String msg) {
        if (IS_DISPLAY_LOG) {
            Log.e(TAG, msg);
        }
    }

    public static void error(String msg, Throwable tr) {
        if (IS_DISPLAY_LOG) {
            Log.e(TAG, msg, tr);
        }
    }

    public static void println(Object obj) {
        if (IS_DISPLAY_LOG) {
            System.out.println(obj);
        }
    }
}
