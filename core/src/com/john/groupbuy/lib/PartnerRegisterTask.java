package com.john.groupbuy.lib;

import java.io.IOException;

import android.os.AsyncTask;

import com.john.groupbuy.lib.http.RegisterResult;
import com.john.util.HttpResponseException;
import com.john.util.LogUtil;

public class PartnerRegisterTask extends AsyncTask<String, Void, RegisterResult> {

    private HttpEventListenter mListener;

    public PartnerRegisterTask(HttpEventListenter listener) {
        super();
        mListener = listener;
    }

    @Override
    protected RegisterResult doInBackground(String... params) {
        final String username = params[0];
        final String password = params[1];
        final String email = params[2];
        RegisterResult res = null;
        try {
            res = FactoryCenter.getUserInfoCenter().userRegister(username, password, "", email, "");
        } catch (HttpResponseException e) {
            LogUtil.warn(e.getMessage(), e);
        } catch (IOException e) {
            LogUtil.warn(e.getMessage(), e);
        }
        return res;
    }

    @Override
    protected void onPreExecute() {
        // showDialog(DIALOG_LOGIN);
        if (mListener != null) {
            mListener.HttpRequestStart();
        }
    }

    @Override
    protected void onPostExecute(RegisterResult result) {
        if (result != null) {

        } else {

        }
        if (mListener != null) {
            mListener.HttpRequestEnd();
        }
    }

}
