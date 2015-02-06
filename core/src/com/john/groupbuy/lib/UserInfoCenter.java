package com.john.groupbuy.lib;

import android.text.TextUtils;
import com.john.groupbuy.CacheManager;
import com.john.groupbuy.lib.http.*;
import com.john.util.HttpResponseException;
import com.john.util.HttpUtil;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class UserInfoCenter {
    /**
     * @param account
     *            账号
     * @param password
     *            密码
     * @return
     * @throws IOException
     * @throws HttpResponseException
     * @throws ClientProtocolException
     */
    public LoginResult userLogin(String account, String password) throws HttpResponseException, IOException {
        if (TextUtils.isEmpty(account) && TextUtils.isEmpty(password)) {
            String url = Interface.S_USER_LOGIN;
            return HttpUtil.get(url, LoginResult.class);
        }
        String url = "";
        if (CacheManager.getInstance().isZuitu()) {
            url = Interface.S_USER_LOGIN + "name=" + account + "&pwd=" + password;
        } else {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("name", account));
            nameValuePairs.add(new BasicNameValuePair("pwd", password));
            String params = URLEncodedUtils.format(nameValuePairs, "UTF-8");
            url = Interface.S_USER_LOGIN + params;
        }
        return HttpUtil.get(url, LoginResult.class);
    }

    public LoginResult partnerLogin(String account, String password) throws HttpResponseException, IOException {
        account = TextUtils.isEmpty(account) ? "" : account;
        password = TextUtils.isEmpty(password) ? "" : password;
        String url = Interface.S_PARTNER_LOGIN + "name=" + account + "&pwd=" + password;
        LoginResult loginResult = HttpUtil.partnerGet(url, LoginResult.class);
        return loginResult;
    }

    public StatusInfo partnerLogout() throws ParseException, IOException, HttpResponseException {
        String url = Interface.S_PARTNER_LOGOUT;
        return HttpUtil.get(url, StatusInfo.class);
    }

    public RegisterResult userRegister(String account, String password, String phoneCode, String email, String phoneNum)
            throws HttpResponseException, IOException {
        String url = "";
        if (CacheManager.getInstance().isZuitu()) {
            url = Interface.S_USER_REGISTER + "name=" + account + "&pwd=" + password + "&email=" + email;
            if (!TextUtils.isEmpty(phoneCode)) {
                url += ("&vcode=" + phoneCode);
            }
            if (!TextUtils.isEmpty(phoneNum)) {
                url += ("&mobile=" + phoneNum);
            }
        } else {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("name", account));
            nameValuePairs.add(new BasicNameValuePair("pwd", password));
            nameValuePairs.add(new BasicNameValuePair("email", email));
            if (!TextUtils.isEmpty(phoneCode)) {
                nameValuePairs.add(new BasicNameValuePair("vcode", phoneCode));
            }
            if (!TextUtils.isEmpty(phoneNum)) {
                nameValuePairs.add(new BasicNameValuePair("mobile", phoneNum));
            }
            url = Interface.S_USER_REGISTER + URLEncodedUtils.format(nameValuePairs, "UTF-8");
        }
        RegisterResult res = HttpUtil.get(url, RegisterResult.class);
        return res;
    }

    public CouponListResult getCouponList(int type) throws HttpResponseException, IOException {
        String url = Interface.S_USERGET_COUPON + "type=" + type;

        CouponListResult res = HttpUtil.get(url, CouponListResult.class);
        return res;
    }
}
