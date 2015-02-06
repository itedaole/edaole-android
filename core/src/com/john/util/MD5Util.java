/**
 * 
 */

package com.john.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.text.TextUtils;

/**
 * MD5工具类
 * 
 * @author zhaozhongyang
 */
public class MD5Util {

    /**
     * 加密
     * 
     * @param plainText
     * @return
     */
    public static String encoder(String plainText) {
        if (TextUtils.isEmpty(plainText)) {
            return "";
        }
        String str = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            str = buf.toString();/* 32位的加密 */
            str = str.substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return str;
    }

}
