package com.john.groupbuy;

import java.io.IOException;

import org.apache.http.ParseException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.groupbuy.yidaole.wxapi.WXEntryActivity;
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.GlobalKey;
import com.john.groupbuy.lib.http.LoginResult;
import com.john.groupbuy.zxing.FinishListener;
import com.john.util.HttpResponseException;
import com.john.util.HttpUtil;
import com.john.util.LogUtil;
import com.john.util.Utility;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

public class LoginView extends RelativeLayout implements IWXAPIEventHandler{
	public interface OnLoginListener {
		void loginSuccess();

		void loginFailuer();

		void bindingSuccess();

		void bindingFailure();
	}

	final static protected int USER_LOGIN = 0;
	final static protected int PARTNER_LOGIN = 1;
	final static protected int VERIFY_LOGIN = 2;
	
	
	
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private OnLoginListener mOnLoginListener;
	public ProgressDialog myDialog;
	private LoginTask mLoginTask;
	private AutoCompleteTextView mAccount;
	private EditText mPassword;
	private TextView mRegisterBtn;
	private Button mLoginBtn;
	private TextView mTencentLoginBtn;
	private String mName;
	private String mPwd;
	private Activity mActivity;
	private int mStatus;
	
	private TextView wxLoginBtn;

	public void setActivity(Activity activity) {
		this.mActivity = activity;
	}

	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.loginbtn) {
				loginBtn();
			} else if (v.getId() == R.id.registerbtn) {
				registerBtn();
			} else if (v.getId() == R.id.qqloginBtn) {
				qqLogin();
			} else if (v.getId() == R.id.wxloginBtn) {
				wxLogin();
			}

		}
	};

	private void loginBtn() {
		mName = mAccount.getText().toString();
		mPwd = mPassword.getText().toString();
		if (!TextUtils.isEmpty(mName) && !TextUtils.isEmpty(mPwd)) {
			if (mLoginTask != null) {
				mLoginTask.cancel(true);
				mLoginTask = null;
			}
			mLoginTask = (LoginTask) new LoginTask().execute(mName, mPwd);
		} else {
			Toast.makeText(mContext, R.string.toast_input_account_or_pwd,
					Toast.LENGTH_SHORT).show();
		}

	}

	private void registerBtn() {
		Intent intent = new Intent(mContext, RegisterActivity.class);
		mContext.startActivity(intent);
	}

	public LoginView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	public LoginView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public LoginView(Context context, boolean isUserLoging) {
		super(context);
		mContext = context;
		if (isUserLoging)
			mStatus = USER_LOGIN;
		else
			mStatus = PARTNER_LOGIN;

		init();
	}

	public void setLoginUser(boolean value) {
		if (value)
			mStatus = USER_LOGIN;
		else
			mStatus = PARTNER_LOGIN;
	}

	private void init() {
		mLayoutInflater = LayoutInflater.from(mContext);
		removeAllViews();
		LayoutParams paramsItem = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		View loginView = mLayoutInflater.inflate(R.layout.login_view, null);
		addView(loginView, paramsItem);
		mAccount = (AutoCompleteTextView) loginView
				.findViewById(R.id.account_pe);
		mPassword = (EditText) loginView.findViewById(R.id.password);
		mLoginBtn = (Button) loginView.findViewById(R.id.loginbtn);
		mLoginBtn.setOnClickListener(mClickListener);

		mRegisterBtn = (TextView) loginView.findViewById(R.id.registerbtn);
		mRegisterBtn.setOnClickListener(mClickListener);
		mTencentLoginBtn = (TextView) loginView
				.findViewById(R.id.qqloginBtn);
		mTencentLoginBtn.setOnClickListener(mClickListener);
		if (mStatus == PARTNER_LOGIN) {
			mRegisterBtn.setVisibility(View.INVISIBLE);
			mTencentLoginBtn.setVisibility(View.INVISIBLE);
		}

//        if (getContext().getPackageName().contains("zhongtuanwang")){
//            mTencentLoginBtn.setVisibility(INVISIBLE);
//        }
		wxLoginBtn = (TextView)loginView.findViewById(R.id.wxloginBtn);
		wxLoginBtn.setOnClickListener(mClickListener);
	}

	public void setOnLoginSuccess(OnLoginListener listener) {
		mOnLoginListener = listener;
	}

	protected void qqLogin() {
		Intent intent = new Intent(mActivity, TencentLoginActivity.class);
		mActivity.startActivityForResult(intent,
				GlobalKey.REQUEST_TENCENT_BINDING);
	}
	
	protected void wxLogin() {
		
		//注册
		WXEntryActivity.api.registerApp(WXEntryActivity.WEIXINAPPID);
		//获取
		final SendAuth.Req req = new SendAuth.Req();
		req.scope = "snsapi_userinfo";
		req.state = "none";
		WXEntryActivity.api.sendReq(req);
	}

	private class WXLoginTask extends AsyncTask<String, Void, LoginResult> {
		public WXLoginTask() {
			super();
		}
		@Override
		protected void onPreExecute() {
			myDialog = new ProgressDialog(mActivity);
			myDialog.setIndeterminate(true);
			myDialog.setMessage("登录中...");
			myDialog.show();
		}
		@Override
		protected LoginResult doInBackground(String... params) {
			if (params.length != 1)
		        return null;
		    try {
		    	return HttpUtil.get(params[0],LoginResult.class);
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
		protected void onPostExecute(LoginResult result) {
			super.onPostExecute(result);
			if (myDialog != null) {
				myDialog.dismiss();
				myDialog = null;
			}
			if (result != null && result.mResultInfo != null) {
				if (mStatus != PARTNER_LOGIN) {
					GroupBuyApplication.sIsUserLogin = true;
					GroupBuyApplication.sIsPartnerLogin = false;
					GroupBuyApplication.sBindingPhone = result.mResultInfo.mobile;
					GroupBuyApplication.sUserInfo = result.mResultInfo;
					writeSharePreferData();
				} else {
					GroupBuyApplication.sIsPartnerLogin = true;
					GroupBuyApplication.sIsUserLogin = false;
				}
				notifyLoginSuccess();
				return;
			}

			notifyLoginFailure();
		}
	}
	
	
	private class LoginTask extends AsyncTask<String, Void, LoginResult> {

		public LoginTask() {
			super();
		}

		@Override
		protected void onPreExecute() {
			myDialog = new ProgressDialog(mActivity);
			myDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					mLoginTask.cancel(true);
				}
			});
			myDialog.setIndeterminate(true);
			if (mStatus == VERIFY_LOGIN)
				myDialog.setMessage("正在验证QQ登录");
			else
				myDialog.setMessage("登录中...");
			myDialog.setCancelable(true);
			myDialog.show();
		}

		@Override
		protected LoginResult doInBackground(String... params) {
			final String username = params[0];
			final String password = params[1];

			LoginResult loginResult = null;
			try {
				if (mStatus == PARTNER_LOGIN) {
					loginResult = FactoryCenter.getUserInfoCenter()
							.partnerLogin(username, password);
				} else {
					loginResult = FactoryCenter.getUserInfoCenter()
							.userLogin(username, password);
				}
			} catch (HttpResponseException e) {
				LogUtil.warn(e.getMessage(), e);
			} catch (IOException e) {
				LogUtil.warn(e.getMessage(), e);
			}
			return loginResult;
		}

		@Override
		protected void onPostExecute(LoginResult result) {
			if (myDialog != null) {
				myDialog.dismiss();
				myDialog = null;
			}

			if (result != null && result.mResultInfo != null) {
				if (mStatus != PARTNER_LOGIN) {
					GroupBuyApplication.sIsUserLogin = true;
					GroupBuyApplication.sIsPartnerLogin = false;
					GroupBuyApplication.sBindingPhone = result.mResultInfo.mobile;
					GroupBuyApplication.sUserInfo = result.mResultInfo;
					writeSharePreferData();
				} else {
					GroupBuyApplication.sIsPartnerLogin = true;
					GroupBuyApplication.sIsUserLogin = false;
				}
				notifyLoginSuccess();
				return;
			}

			notifyLoginFailure();
		}
	}

	public void verifyBindingResult() {
		mStatus = VERIFY_LOGIN;
		if (mLoginTask != null) {
			mLoginTask.cancel(true);
			mLoginTask = null;
		}
		mLoginTask = (LoginTask) new LoginTask().execute("", "");
	}

	protected void notifyLoginSuccess() {
		if (mOnLoginListener == null)
			return;

		if (mStatus == VERIFY_LOGIN) {
			mOnLoginListener.bindingSuccess();
		} else {
			mOnLoginListener.loginSuccess();
		}
	}

	protected void notifyLoginFailure() {

		if (mOnLoginListener == null)
			return;

		if (mStatus == VERIFY_LOGIN) {
			mOnLoginListener.bindingFailure();
		} else {
			mOnLoginListener.loginFailuer();
		}
	}

	protected void writeSharePreferData() {
		Utility.writeUserConfig(getContext(), mName, mPwd);
	}

	@Override
	public void onReq(BaseReq arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResp(BaseResp resp)
	{
		int result = 0;
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			if(resp instanceof SendAuth.Resp)
			{
				mStatus = USER_LOGIN;
				new WXLoginTask().execute("http://www.edaole.com/thirdpart/wechat/app_callback.php?"+"code="+((SendAuth.Resp)resp).code);
			}
			else
			{
				((Activity)getContext()).finish();
			}
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = R.string.errcode_cancel;
			Toast.makeText(mContext, "BaseResp.ErrCode:"+result, Toast.LENGTH_LONG).show();
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = R.string.errcode_deny;
			Toast.makeText(mContext, "BaseResp.ErrCode:"+result, Toast.LENGTH_LONG).show();
			break;
		default:
			result = R.string.errcode_unknown;
			break;
		}
	}
}
