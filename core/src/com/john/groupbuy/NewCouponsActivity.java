package com.john.groupbuy;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.http.ParseException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.groupbuy.yidaole.CouponActivity;
import com.john.groupbuy.adapter.NewCouponListAdapter;
import com.john.groupbuy.lib.http.GlobalKey;
import com.john.groupbuy.lib.http.Interface;
import com.john.groupbuy.lib.http.NewCouponListResult;
import com.john.groupbuy.lib.http.NewCouponListResult_Result;
import com.john.groupbuy.lib.http.SingleCouponInfo;
import com.john.util.HttpResponseException;
import com.john.util.HttpUtil;

public class NewCouponsActivity extends BaseActivity implements
OnItemClickListener {
	private ListView mListView;
	private NewCouponListAdapter mAdapter;
	private CouponsTask couponsTask;
	private LoadingView loadingView;

	List<NewCouponListResult_Result> couponList;


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
	}

	protected void initViewComponents(Bundle savedInstanceState) {
		mListView = (ListView) findViewById(R.id.listview);
		loadingView = (LoadingView) findViewById(R.id.loading_view);
		String title = "我的优惠劵";
		
		setTitle(title);
			mListView.setOnItemClickListener(this);
		request();
	}

	private void request() {
		String params = String.format(Locale.getDefault(),
                "User/getCouponNew&user_id=%1$s",
                GroupBuyApplication.sUserInfo.id);
		System.out.println("request_url");
        String url = Interface.DEFAULT_APP_HOST + params;
        couponsTask = new CouponsTask();
        couponsTask.execute(url);
		loadingView.showMessage(R.string.loading_data_hint, true);
	}


	protected void showNoDataView() {
		loadingView.showMessage(R.string.no_data_tips, false);
		mListView.setVisibility(View.GONE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		/*
		SingleCouponInfo info = (SingleCouponInfo) parent.getAdapter().getItem(
				position);
		if (info != null) {
			Intent intent = new Intent(NewCouponsActivity.this,
					OrderDetailActivity.class);
			intent.putExtra(GlobalKey.PARCELABLE_KEY, info);
			startActivity(intent);
		}*/
		NewCouponListResult_Result info = (NewCouponListResult_Result) parent.getAdapter().getItem(
				position);
		
		String params = String.format(Locale.getDefault(),
                "Tuan/detail_new&id=%1$s&user_id=%2$s",
                info.id, GroupBuyApplication.sUserInfo.id);
        String url = Interface.DEFAULT_APP_HOST + params;
        CouponTask couponTask = new CouponTask();
        couponTask.execute(url);
	}

	@Override
	protected void onDestroy() {
		if(couponsTask != null && couponsTask.getStatus() != Status.FINISHED)
			couponsTask.cancel(true);
		super.onDestroy();
	}

	private final class CouponsTask extends
	AsyncTask<String,Void,NewCouponListResult> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected NewCouponListResult doInBackground(String... params) {
		    if (params.length != 1)
		        return null;
		    try {
		        return HttpUtil.get(params[0],NewCouponListResult.class);
		    } catch (ParseException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    } catch (HttpResponseException e) {
		        e.printStackTrace();
		    }
		
		    return null;
		}
		
		@Override
		protected void onPostExecute(NewCouponListResult result) {
		    super.onPostExecute(result);
		    System.out.println(result.status);
		    if(result.status==1)
		    {
		    	couponList = result.result;
				if(couponList != null){
					mAdapter = new NewCouponListAdapter(NewCouponsActivity.this,couponList);
					mListView.setAdapter(mAdapter);
					if(couponList.isEmpty())
						showNoDataView();
					else
						loadingView.setVisibility(View.GONE);
					return;
				}
		    }
		    
		}
	}
	
	
	
	

    private ProgressDialog progressDialog;
	private final class CouponTask extends
	AsyncTask<String,Void,SingleCouponInfo> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialog.show(NewCouponsActivity.this, "", "");
		}
		
		@Override
		protected SingleCouponInfo doInBackground(String... params) {
		    if (params.length != 1)
		        return null;
		    try {
		        return HttpUtil.get(params[0], SingleCouponInfo.class);
		    } catch (ParseException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    } catch (HttpResponseException e) {
		        e.printStackTrace();
		    }
		
		    return null;
		}
		
		@Override
		protected void onPostExecute(SingleCouponInfo result) {
		    super.onPostExecute(result);
		    progressDialog.dismiss();
		    if(result.status==1)
		    {
		    	Intent intent = new Intent(NewCouponsActivity.this,CouponActivity.class).putExtra("SingleCouponInfo", result).putExtra("Dismiss", true);
		        startActivityForResult(intent, GlobalKey.REQUEST_PAYMENT);
		    }
		    
		}
		
		}
	
	
}
