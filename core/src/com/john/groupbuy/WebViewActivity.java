package com.john.groupbuy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar.LayoutParams;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class WebViewActivity extends BaseActivity implements
		OnLoadingListener {
    
    public static final String KEY_TITLE = "key_title";
    public static final String KEY_LOADING_URL = "key_url";

	private WebView mWebView;
	private ProgressBar progressBar;
	private String loadingUrl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.product_details_activity);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		enableBackBehavior();
		resolveIntent();
		initViewComponents();
		initComponentsData();
	}
	
	private void resolveIntent(){
	    Intent intent = getIntent();
	    if(intent == null){
	        finish();
	        return;
	    }
	    String title = intent.getStringExtra(KEY_TITLE);
	    setTitle(title);
	    loadingUrl = intent.getStringExtra(KEY_LOADING_URL);
	}

	@SuppressLint("SetJavaScriptEnabled")
    protected void initViewComponents() {
		mWebView = (WebView) findViewById(R.id.webView);
		WebSettings settings = mWebView.getSettings();
		settings.setSupportZoom(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setBuiltInZoomControls(true);
		settings.setJavaScriptEnabled(true);
		// settings.setPluginsEnabled(true);// support flash
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(false);
		// settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

		MyWebViewClient client = new MyWebViewClient(this);
		mWebView.setWebViewClient(client);

		progressBar = (ProgressBar) getLayoutInflater().inflate(R.layout.item_progressbar, null);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.RIGHT|Gravity.CENTER_VERTICAL;
		getSupportActionBar().setCustomView(progressBar, params);
	}

	protected void initComponentsData() {
		if (loadingUrl != null && loadingUrl.length() > 5) {
			mWebView.loadUrl(loadingUrl);
		}
	}

	@Override
	public void onStartLoad() {
	}

	@Override
	public void onFinishLoad() {
		progressBar.setVisibility(View.INVISIBLE);
	}

}
