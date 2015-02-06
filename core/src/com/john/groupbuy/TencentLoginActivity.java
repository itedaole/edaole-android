package com.john.groupbuy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v7.app.ActionBar.LayoutParams;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.john.groupbuy.lib.http.Interface;
import com.john.util.HttpUtil;

import java.io.*;
import java.net.URLDecoder;

public class TencentLoginActivity extends BaseActivity implements Callback {

    private static final String TENCENT_LOG = "tencent_log.txt";
    private WebView myWebView;
    private final static String OAUTH_URL = "https://graph.qq.com/oauth2.0/authorize?";
    private final static String REDIRECT_URI_START = "redirect_uri=";
    private final static String REDIRECT_URI_END = "&";
    private final static String FW_SUCCESS = Interface.DEFAULT_APP_HOST+"/Index/result&status=1";
    private final static String FW_FAILURE = Interface.DEFAULT_APP_HOST+"/Index/result&status=0";
    private String callback;

    private boolean bindCompeted = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler;
    private ProgressBar progressBar;

    private FileOutputStream outPutStream;


    private WebViewClient myClient = new WebViewClient() {
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("onPageStarted", url);
            progressBar.setVisibility(View.VISIBLE);
            if (outPutStream != null) {
                try {
                    outPutStream.write(url.getBytes());
                    outPutStream.write('\n');
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!CacheManager.getInstance().isZuitu()){
                return;
            }

            if (urlStartWith(url, OAUTH_URL) && callback == null) {
                // find callback url from it
                int startIndex = url.indexOf(REDIRECT_URI_START);
                if (startIndex == -1) {
                    notifyCompleted();
                    return;
                }
                int endIndex = url.indexOf(REDIRECT_URI_END, startIndex);
                if (endIndex == -1) {
                    notifyCompleted();
                    return;
                }

                String cb = url.substring(
                        startIndex + REDIRECT_URI_START.length(), endIndex);
                try {
                    callback = URLDecoder.decode(cb, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (TextUtils.isEmpty(callback)) {
                    notifyCompleted();
                }
            } else if (urlStartWith(url, callback)) { // start with
                bindCompeted = true;
                return;
            }

            if (bindCompeted && urlStartWith(url,Interface.S_HOME_PAGE)){
                notifyCompleted();
            }
        }

        private void notifyCompleted() {
            myWebView.stopLoading();
            Message msg = new Message();
            msg.what = 1;
            mHandler.sendMessageDelayed(msg, 1000);
            progressBar.setVisibility(View.INVISIBLE);
        }


        public void onPageFinished(WebView view, String url) {
            Log.d("onPageFinished", url);
            progressBar.setVisibility(View.INVISIBLE);
            if (CacheManager.getInstance().isZuitu())
                return;
            if (urlStartWith(url,FW_SUCCESS))
                notifyCompleted();
            else if(urlStartWith(url,FW_FAILURE))
                notifyCompleted();
        }
    };

    private boolean urlStartWith(String url, String targetString) {
        if (url == null || targetString == null) {
            return false;
        }
        return url.startsWith(targetString);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tencent_login);
        initViewComponents();
        initComponentsData();
        mHandler = new Handler(this);
        enableBackBehavior();
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        setTitle(R.string.title_tencent_login);

        File file = new File(Environment.getExternalStorageDirectory(),
                TENCENT_LOG);
        try {
            outPutStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    protected void initViewComponents() {
        myWebView = (WebView) findViewById(R.id.webView);

        progressBar = (ProgressBar) getLayoutInflater().inflate(
                R.layout.item_progressbar, null);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        getSupportActionBar().setCustomView(progressBar, params);
    }

    protected void initComponentsData() {
        WebSettings settings = myWebView.getSettings();
        settings.setSupportZoom(true);
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setBuiltInZoomControls(true);// support zoom
        // settings.setPluginsEnabled(true);// support flash
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(false);
        myWebView.setWebViewClient(myClient);
        if (CacheManager.getInstance().isZuitu())
            myWebView.loadUrl(Interface.S_TENCENT_BINDING);
        else
            myWebView.loadUrl(Interface.DEFAULT_APP_HOST+"/Index/QQLogin");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up
        // to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (outPutStream != null) {
            try {
                outPutStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    public boolean handleMessage(Message msg) {
        CookieManager manager = CookieManager.getInstance();
        String cookieString = manager.getCookie(Interface.DNS_NAME);
        HttpUtil.setCookie(cookieString);
        Activity activity = TencentLoginActivity.this;
        activity.setResult(RESULT_OK);
        activity.finish();
        return true;
    }

}
