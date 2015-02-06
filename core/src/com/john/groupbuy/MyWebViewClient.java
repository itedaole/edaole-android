package com.john.groupbuy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebViewClient extends WebViewClient {

	private OnLoadingListener mListener;

	public MyWebViewClient(OnLoadingListener l) {
		super();
		setListener(l);
	}

	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		super.onPageStarted(view, url, favicon);
		if (mListener != null)
			mListener.onStartLoad();
	}

	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
		if (mListener != null)
			mListener.onFinishLoad();
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		if (url.startsWith("tel:")) {
			Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
			view.getContext().startActivity(intent);
			return true;
		}
		return false;
	}

	public void setListener(OnLoadingListener l) {
		mListener = l;
	}

}
