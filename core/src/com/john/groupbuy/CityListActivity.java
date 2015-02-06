package com.john.groupbuy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.john.groupbuy.adapter.CityListAdapter;
import com.john.groupbuy.citylist.PinyinComparator;
import com.john.groupbuy.citylist.SideBar;
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.CityItem;
import com.john.groupbuy.lib.http.CityListInfo;
import com.john.groupbuy.lib.http.GlobalKey;
import com.john.util.CharacterParser;
import com.john.util.HttpResponseException;
import com.john.util.LogUtil;
import com.umeng.analytics.MobclickAgent;

public class CityListActivity extends BaseActivity implements
		OnItemClickListener, OnClickListener {
	
	public final static String KEY_SELECT_CITY_MODE = "key_select_city_mode";
	
	private ListView mListView;
	private AsyncTask<String, Void, CityListInfo> mTask;
	private ConvertCityTask mConvertTask;
	private CityListAdapter mAdapter;
	private List<CityItem> mCityList;
	private SideBar indexBar;
	private WindowManager mWindowManager;
	private TextView mDialogText;
	private TextView mAllCityView;
	
	private LoadingView loadingView;
	private boolean selectCityMode = false;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_citiy_list);
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		enableBackBehavior();
		setTitle(R.string.title_choose_city);
		initView();

		mCityList = ProductListFragment.getCityList();
		if (mCityList == null) {
			mCityList = new ArrayList<CityItem>();
			Request();
		} else {
			loadingView.setVisibility(View.GONE);
			mAdapter = new CityListAdapter(CityListActivity.this, mCityList);
			mListView.setAdapter(mAdapter);
		}
		
		resolveIntent(savedInstanceState);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.putBoolean(KEY_SELECT_CITY_MODE, selectCityMode);
	}

	
	private void resolveIntent(Bundle savedInstanceState){
		
		if(savedInstanceState == null){
			Intent intent = getIntent();
			if(intent == null)
				return;
			selectCityMode = intent.getBooleanExtra(KEY_SELECT_CITY_MODE, false);
		}else{
			selectCityMode = savedInstanceState.getBoolean(KEY_SELECT_CITY_MODE, false);
		}
		if(selectCityMode){
			setTitle(R.string.select_city_hint);
		}
	}
	
	private void initView() {
		mAllCityView = (TextView) findViewById(R.id.all_city);
		mAllCityView.setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.city_list_view);
		mListView.setOnItemClickListener(this);
		indexBar = (SideBar) findViewById(R.id.sideBar);
		indexBar.setListView(mListView);
		mDialogText = (TextView) LayoutInflater.from(this).inflate(
				R.layout.list_position, null);
		mDialogText.setVisibility(View.INVISIBLE);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
						| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		mWindowManager.addView(mDialogText, lp);
		indexBar.setTextView(mDialogText);
		
		loadingView = (LoadingView) findViewById(R.id.loading_view);
		loadingView.showMessage(R.string.loading_city_list_hint, true);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		CityItem item = (CityItem) arg0.getAdapter().getItem(arg2);
		Intent intent = new Intent();
		// intent.putExtra("city", mAdapter.getCityNameByIndex(arg2));
		// intent.putExtra("id", mAdapter.getCityIdByIndex(arg2));
		intent.putExtra("city", item.getName());
		intent.putExtra("id", item.getId());
		setResult(RESULT_OK, intent);
		finish();

	}

	private void Request() {

		if (mCityList.size() != 0)
			return;

		mTask = new CityListTask();
		mTask.execute();
	}

	private class CityListTask extends AsyncTask<String, Void, CityListInfo> {

		public CityListTask() {
			super();
		}

		@Override
		protected CityListInfo doInBackground(String... params) {
			CityListInfo res = null;
			try {
				res = FactoryCenter.getProcessCenter().getCityListInfo();
			} catch (HttpResponseException e) {
				LogUtil.warn(e.getMessage(), e);
			} catch (IOException e) {
				LogUtil.warn(e.getMessage(), e);
			}
			return res;
		}

		@Override
		protected void onPostExecute(CityListInfo result) {
			if(result == null || result.result == null){
				loadingView.showMessage("获取城市列表失败", false);
				return;
			}
			mCityList.addAll(result.result);
			mConvertTask = new ConvertCityTask();
			mConvertTask.execute();
		}
	}

	private class ConvertCityTask extends AsyncTask<String, Void, Void> {


		@Override
		protected Void doInBackground(String... params) {
			if (mCityList == null || mCityList.size() <= 0) {
				return null;
			}
			CharacterParser parser = CharacterParser.getInstance();
			for (CityItem item : mCityList) {
				String pingyin = parser.getSelling(item.getName());
				item.setPingying(pingyin);
			}
			Collections.sort(mCityList, new PinyinComparator());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			ProductListFragment.setCityList(mCityList);
			mAdapter = new CityListAdapter(CityListActivity.this, mCityList);
			mListView.setAdapter(mAdapter);
			loadingView.setVisibility(View.GONE);
		}
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.all_city) {
			Intent intent = new Intent();
			intent.putExtra("city", "全部");
			intent.putExtra("id", GlobalKey.ALL_CITY_ID);
			setResult(RESULT_OK, intent);
			finish();
		}
	}
	
	@Override
	public void onBackPressed() {
		if(selectCityMode)
			setResult(RESULT_CANCELED);
		super.onBackPressed();
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		mWindowManager.removeViewImmediate(mDialogText);
		indexBar.setTextView(null);
		mDialogText = null;
		mWindowManager = null;
		super.onDestroy();
	}
}
