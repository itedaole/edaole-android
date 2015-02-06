package com.john.groupbuy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.groupbuy.yidaole.CouponActivity;
import com.groupbuy.yidaole.wxapi.WXEntryActivity;
import com.john.groupbuy.lib.http.GlobalKey;
import com.john.groupbuy.lib.http.Interface;
import com.john.groupbuy.lib.http.NewCouponListResult;
import com.john.groupbuy.lib.http.UserInfo;
import com.john.util.HttpResponseException;
import com.john.util.HttpUtil;
import com.john.util.Utility;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class UserHomeFragment extends Fragment implements OnClickListener {

	private LinearLayout notLoginLayout;
	private LinearLayout hasLoginLayout;
	private Button logoutBtn;

	private TextView nameLabel;
	private TextView balanceLabel;

	private int currentButtonId;

	private ProgressDialog waitDialog;

	private LogoutTask taskLogout;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
		BaseActivity activity = (BaseActivity) getActivity();
		activity.getSupportActionBar().setDisplayShowCustomEnabled(false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activity.setTitle(R.string.user_home_page_title);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden){
			getActivity().setTitle(R.string.user_home_page_title);
			BaseActivity activity = (BaseActivity) getActivity();
			activity.getSupportActionBar().setDisplayShowCustomEnabled(false);			
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	    
		View rootView = inflater
				.inflate(R.layout.activity_user_home_page, null);
		initViewComponets(rootView);
		updateLayout();
		return rootView;
	}

	protected void initViewComponets(View rootView) {
		notLoginLayout = (LinearLayout) rootView
				.findViewById(R.id.has_not_login_layout);
		Button loginButton = (Button) notLoginLayout.findViewById(R.id.login);
		loginButton.setOnClickListener(this);

		hasLoginLayout = (LinearLayout) rootView
				.findViewById(R.id.has_login_layout);
		logoutBtn = (Button) rootView.findViewById(R.id.logout_btn);
		logoutBtn.setOnClickListener(this);

		nameLabel = (TextView) hasLoginLayout.findViewById(R.id.username);
		balanceLabel = (TextView) hasLoginLayout.findViewById(R.id.balance);
		
		rootView.findViewById(R.id.list_title).setOnClickListener(this);
/*
		TextView unused = (TextView) rootView.findViewById(R.id.unused_orders);
		unused.setOnClickListener(this);

		TextView used = (TextView) rootView.findViewById(R.id.used_order);
		used.setOnClickListener(this);

		TextView out_date = (TextView) rootView.findViewById(R.id.out_date);
		out_date.setOnClickListener(this);

		TextView express = (TextView) rootView.findViewById(R.id.express_btn);
		express.setOnClickListener(this);

		TextView orderButton = (TextView) rootView.findViewById(R.id.order_btn);
		orderButton.setOnClickListener(this);
*/
	}

	protected void updateLayout() {
		if (GroupBuyApplication.sIsUserLogin) {
			notLoginLayout.setVisibility(View.GONE);
			hasLoginLayout.setVisibility(View.VISIBLE);
			UserInfo userInfo = GroupBuyApplication.sUserInfo;
			nameLabel.setText(getString(R.string.userName) + userInfo.getUsername());
			balanceLabel.setText(getString(R.string.account_balance)
					+ userInfo.getMoney());
		} else {
			notLoginLayout.setVisibility(View.VISIBLE);
			hasLoginLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(getActivity());
		updateLayout();
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(getActivity());
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.login) {
			Intent intent = new Intent(getActivity(), WXEntryActivity.class);
			startActivityForResult(intent, 1);
		} else if (v.getId() == R.id.logout_btn) {
			if (taskLogout != null && taskLogout.getStatus() != Status.FINISHED) {
				taskLogout.cancel(true);
			}
			taskLogout = new LogoutTask();
			taskLogout.execute();
		} else {
			//login(v.getId());
			if (isLogin()) {
				/*
				Intent intent = new Intent(getActivity(), CouponsActivity.class);
				intent.putExtra(CouponsActivity.TitleNameKey, "我的优惠劵");
				intent.putExtra(CouponsActivity.RequestTypeKey, 0);
				intent.putExtra(CouponsActivity.ItemClickable, true);
				startActivity(intent);
				return;*/
				
				Intent intent = new Intent(getActivity(), NewCouponsActivity.class);
				startActivity(intent);
				return;
			}
			//currentButtonId = buttonId;
			login();
		}
	}
/*
	protected void onButtonClicked(int buttonId) {
		if (R.id.unused_orders == buttonId) {
			showCouponsActivity(0, "未使用", true);
		} else if (R.id.used_order == buttonId) {
			showCouponsActivity(1, "已使用", false);
		} else if (R.id.out_date == buttonId) {
			showCouponsActivity(2, "已过期", false);
		} else if (R.id.express_btn == buttonId) {
			showExpressActivity();
		} else if (R.id.order_btn == buttonId) {
			showOrderActivity();
		}
	}
*/
	protected void showCouponsActivity(int type, String title, boolean clickable) {
		Intent intent = new Intent(getActivity(), CouponsActivity.class);
		intent.putExtra(CouponsActivity.TitleNameKey, title);
		intent.putExtra(CouponsActivity.RequestTypeKey, type);
		intent.putExtra(CouponsActivity.ItemClickable, clickable);
		startActivity(intent);
	}

	protected void showExpressActivity() {
		Intent intent = new Intent(getActivity(), ExpressActivity.class);
		startActivity(intent);
	}

	protected void showOrderActivity() {
		Intent intent = new Intent(getActivity(), OrderActivity.class);
		startActivity(intent);
	}

	protected void login() {
		Intent intent = new Intent(getActivity(), WXEntryActivity.class);
		startActivityForResult(intent, 2);
	}
/*
	protected void login(int buttonId) {
		if (isLogin()) {
			onButtonClicked(buttonId);
			return;
		}
		currentButtonId = buttonId;
		login();
	}
*/
	protected void logout() {
		GroupBuyApplication.sIsUserLogin = false;
		updateLayout();
		//clear user data
		Utility.writeUserConfig(getActivity(), "", "");
	}

	protected boolean isLogin() {
		return GroupBuyApplication.sIsUserLogin;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK)
			return;

		updateLayout();
		if (requestCode == 2) {
			//onButtonClicked(currentButtonId);
		}
	}

	public class LogoutTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			waitDialog = new ProgressDialog(getActivity());
			waitDialog.setMessage("注销中，请稍后...");
			waitDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (taskLogout.getStatus() != Status.FINISHED)
						taskLogout.cancel(true);
				}
			});
		}

		@Override
		protected String doInBackground(String... arg0) {
			String result = "";
			try {
				result = HttpUtil.get(Interface.S_USER_LOGOUT, String.class);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (HttpResponseException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (!TextUtils.isEmpty(result)) {
				Toast.makeText(getActivity(), "注销成功", Toast.LENGTH_LONG).show();
				logout();
			} else {
				Toast.makeText(getActivity(), "注销失败", Toast.LENGTH_LONG).show();
			}

		}
	}
}
