package com.john.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;


public class GetTimeUtil {

    public static class TimeCompareResult {
        public long remainingDays;
        public long remainingHours;
        public long remainingMinute;
        public long remainingSeconds;
    }

    public static String getDate(String month, String day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 24小时制
        java.util.Date d = new java.util.Date();
        String str = sdf.format(d);
//        String nowmonth = str.substring(5, 7);
        String nowday = str.substring(8, 10);
        String result = null;

        int temp = Integer.parseInt(nowday) - Integer.parseInt(day);
        switch (temp) {
        case 0:
            result = "";
            break;
        default:
            result += (temp + "天");
            break;
        }
        return result;
    }

    public static String getTime(int timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = null;
        try {
            // java.util.Date currentdate = new java.util.Date();// 当前时间
            //
            // long i = (currentdate.getTime() / 1000 - timestamp) / (60);
            // System.out.println(currentdate.getTime());
            // System.out.println(i);
            Timestamp now = new Timestamp(System.currentTimeMillis());// 获取系统当前时间
            System.out.println("now-->" + now);// 返回结果精确到毫秒。

            String str = sdf.format(new Timestamp(IntToLong(timestamp)));
            time = str.substring(11, 16);

            String month = str.substring(5, 7);
            String day = str.substring(8, 10);
            // System.out.println(str);
            // System.out.println(time);
            // System.out.println(getDate(month, day));
            time = getDate(month, day) + time;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    // java Timestamp构造函数需传入Long型
    public static long IntToLong(int i) {
        long result = (long) i;
        result *= 1000;
        return result;
    }

    public static boolean isToday(String strtime) {
        try {
        	
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        	String startTime = sdf.format(new Date(Long.parseLong(strtime) * 1000L));
        	String nowTimer = sdf.format(new Date());
        	if(startTime.equalsIgnoreCase(nowTimer)){
        		return true;
        	}
//            Timestamp now = new Timestamp(System.currentTimeMillis());// 获取系统当前时间
//            Timestamp start = new Timestamp(Long.parseLong(strtime) * 1000L);
//            int year = now.getYear();
//            int month = now.getMonth();
//            int day = now.getDay();
//            int year1 = start.getYear();
//            int month1 = start.getMonth();
//            int day1 = start.getDay();
//            if (year == year1 && month == month1 && day1 == day) {
//                return true;
//            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    // 如果过期传入的时间，
    public static TimeCompareResult currentTimeCompareWith(String strtime) {

        TimeCompareResult tc = new TimeCompareResult();
        try {
            Date now = new Date(Long.parseLong(strtime) * 1000);
            Date date = new Date(System.currentTimeMillis());
            long l = now.getTime() - date.getTime();
            long day = l / (24 * 60 * 60 * 1000);
            long hour = (l / (60 * 60 * 1000) - day * 24);
            long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
            long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
            tc.remainingDays = day;
            tc.remainingHours = hour;
            tc.remainingMinute = min;
            tc.remainingSeconds = s;
        } catch (Exception e) {

        }
        return tc;
    }
}
