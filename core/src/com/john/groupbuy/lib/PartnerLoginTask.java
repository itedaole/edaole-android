package com.john.groupbuy.lib;

import java.io.IOException;

import android.os.AsyncTask;

import com.john.groupbuy.lib.http.LoginResult;
import com.john.util.HttpResponseException;
import com.john.util.LogUtil;

public class PartnerLoginTask extends AsyncTask<String, Void, LoginResult> {

    private HttpEventListenter mListener;

    public PartnerLoginTask(HttpEventListenter listener) {
        super();
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        // showDialog(DIALOG_LOGIN);
        if (mListener != null) {
            mListener.HttpRequestStart();
        }
    }

    @Override
    protected LoginResult doInBackground(String... params) {
        final String username = params[0];
        final String password = params[1];

        LoginResult loginResult = null;
        try {
            loginResult = FactoryCenter.getUserInfoCenter().partnerLogin(username, password);
        } catch (HttpResponseException e) {
            LogUtil.warn(e.getMessage(), e);
        } catch (IOException e) {
            LogUtil.warn(e.getMessage(), e);
        }
        return loginResult;
    }

    @Override
    protected void onPostExecute(LoginResult result) {
        if (result != null) {

        } else {

        }
        if (mListener != null) {
            mListener.HttpRequestEnd();
        }
    }
}
