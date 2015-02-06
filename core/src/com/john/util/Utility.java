package com.john.util;

import com.john.groupbuy.lib.http.GlobalKey;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class Utility {
	public static String trimFloatStringZero(String input) {
		if (TextUtils.isEmpty(input))
			return input;

		if (-1 == input.indexOf("."))
			return input;

		return input.replaceAll("0+$", "").replaceAll("\\.$", "");
	}

	public static void
	        writeUserConfig(Context context, String name, String pwd) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
		        GlobalKey.SHARE_PREFERS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(GlobalKey.USER_NAME, name);
		editor.putString(GlobalKey.USER_PASSWORD, pwd);
		editor.commit();
	}

	public static String[] readUserConfig(Context context) {
		String[] result = new String[2];
		SharedPreferences sharedPreferences = context.getSharedPreferences(
		        GlobalKey.SHARE_PREFERS_NAME, Context.MODE_PRIVATE);
		result[0] = sharedPreferences.getString(GlobalKey.USER_NAME, "");
		result[1] = sharedPreferences.getString(GlobalKey.USER_PASSWORD, "");
		return result;
	}

}
