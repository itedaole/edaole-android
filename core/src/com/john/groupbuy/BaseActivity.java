package com.john.groupbuy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends ActionBarActivity {
	
	enum LoadingType{
		LOADING_TYPE_REFRESHING,
		LOADING_TYPE_MORE
	}
	
	public final int KEY_PAGE_SIZE = 20;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void enableBackBehavior() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return false;
	}

	public boolean isConnectedNetwork() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		if (networkInfo != null) {
			return networkInfo.isAvailable();
		}
		return false;
	}
	
	public void showToast(String text){
		if(TextUtils.isEmpty(text))
			return;
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	public void showToast(int resId){
		showToast(getString(resId));
	}

}
