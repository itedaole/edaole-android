package com.john.util;

import android.content.Context;

public class DensityUtil {     
    /**
	屏幕密度。px和dp之间的转换
	*/


	//根据手机分辨率，从dp的单位转换为px（像素）
    public static int dip2px(Context context, float dpValue) {     
        final float scale = context.getResources().getDisplayMetrics().density;     
        int value=(int) (dpValue * scale + 0.5f);     
        return value;     
    }     
    //根据手机分辨率，从px的单位转换为dp 
    public static int px2dip(Context context, float pxValue) {     
        final float scale = context.getResources().getDisplayMetrics().density;     
        int value=(int)(pxValue / scale + 0.5f);    
        return value;     
    }
    //获取屏幕宽度
    public static int getScreenWidth(Context context) {     
    	return (int)((float)context.getResources().getDisplayMetrics().widthPixels + 0.5f);     
    }
    //获取屏幕高度
    public static int getScreenHeight(Context context) {     
        return (int)((float)context.getResources().getDisplayMetrics().heightPixels + 0.5f);   
    }
    //获取密度
    public static float getDensity(Context context) {     
        return context.getResources().getDisplayMetrics().density;   
    }
} 