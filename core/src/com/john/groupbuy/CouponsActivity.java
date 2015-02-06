package com.john.groupbuy;

import java.io.IOException;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.john.groupbuy.adapter.CouponListAdapter;
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.CouponItemInfo;
import com.john.groupbuy.lib.http.CouponListResult;
import com.john.groupbuy.lib.http.GlobalKey;
import com.john.util.HttpResponseException;
import com.john.util.LogUtil;

public class CouponsActivity extends BaseActivity implements
OnItemClickListener {

	final static public String RequestTypeKey = "RequestTypeKey";
	final static public String TitleNameKey = "TitleNameKey";
	final static public String ItemClickable = "ItemClickeable";

	private ListView mListView;
	private GetCouponListTask mCouponListTask;
	private CouponListAdapter mAdapter;

	private LoadingView loadingView;

	private int requestType;
	private boolean isClickable = false;

	private boolean shouldSaveCache = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coupon_list);
		enableBackBehavior();
		initViewComponents(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(TitleNameKey, getTitle().toString());
		outState.putInt(RequestTypeKey,requestType);
		outState.putBoolean(ItemClickable,isClickable);
	}

	protected void initViewComponents(Bundle savedInstanceState) {
		mListView = (ListView) findViewById(R.id.listview);
		loadingView = (LoadingView) findViewById(R.id.loading_view);
		String title;
		if(savedInstanceState != null){
			title = savedInstanceState.getString(TitleNameKey);
			requestType = savedInstanceState.getInt(RequestTypeKey, 0);
			isClickable = savedInstanceState.getBoolean(ItemClickable,false);
		}else{
			Intent intent = getIntent();
			title = intent.getStringExtra(TitleNameKey);
			requestType = intent.getIntExtra(RequestTypeKey, 0);
			isClickable = intent.getBooleanExtra(ItemClickable, false);
		}

		if(requestType == 0)
			shouldSaveCache = true;
		setTitle(title);
		if (isClickable)
			mListView.setOnItemClickListener(this);
		request(requestType);
	}

	private void request(int type) {

		if(shouldSaveCache && !isConnectedNetwork()){
			List<CouponItemInfo> couponList = CacheManager.getInstance().getCouponList();
			if(couponList != null){
				mAdapter = new CouponListAdapter(CouponsActivity.this,couponList);
				mListView.setAdapter(mAdapter);
				if(couponList.isEmpty())
					showNoDataView();
				else
					loadingView.setVisibility(View.GONE);
				return;
			}
		}

		mCouponListTask = (GetCouponListTask) new GetCouponListTask()
		.execute(type);
		loadingView.showMessage(R.string.loading_data_hint, true);
	}

	private class GetCouponListTask extends
	AsyncTask<Integer, Void, CouponListResult> {

		public GetCouponListTask() {
			super();
		}

		@Override
		protected CouponListResult doInBackground(Integer... params) {
			int type = params[0];
			CouponListResult res = null;
			try {
				res = FactoryCenter.getUserInfoCenter().getCouponList(type);
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
		}

		@Override
		protected void onPostExecute(CouponListResult result) {

			if (result == null || result.result == null) {
				showNoDataView();
				return;
			}

			if (result.result.size() == 0){
				showNoDataView();
				return;
			}

			loadingView.setVisibility(View.GONE);

			if(shouldSaveCache)
				CacheManager.getInstance().setCouponList(result.result);

			if (mAdapter == null) {
				mAdapter = new CouponListAdapter(CouponsActivity.this,
						result.result);
				mListView.setAdapter(mAdapter);
			} else {
				mAdapter.addListData(result.result, true);
			}
		}
	}

	protected void showNoDataView() {
		loadingView.showMessage(R.string.no_data_tips, false);
		mListView.setVisibility(View.GONE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		CouponItemInfo info = (CouponItemInfo) parent.getAdapter().getItem(
				position);
		if (info != null) {
			Intent intent = new Intent(CouponsActivity.this,
					OrderDetailActivity.class);
			intent.putExtra(GlobalKey.PARCELABLE_KEY, info);
			startActivity(intent);
		}
	}

	@Override
	protected void onDestroy() {
		if(mCouponListTask != null && mCouponListTask.getStatus() != Status.FINISHED)
			mCouponListTask.cancel(true);
		super.onDestroy();
	}

}
