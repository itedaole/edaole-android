package com.john.util;

import java.text.SimpleDateFormat;

public class TimeUtil {
    public static String ConverTime(String value) {
        String res = null;
        try {
//            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
            long time = Long.parseLong(value);
            res = format.format(time * 1000L);
        } catch (NumberFormatException e) {
            res = "";
        }
        return res;
    }
}
