package com.groupbuy.yidaole.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.john.groupbuy.BaseActivity;
import com.john.groupbuy.GroupBuyApplication;
import com.john.groupbuy.LoginView;
import com.john.groupbuy.R;
import com.john.groupbuy.LoginView.OnLoginListener;
import com.john.groupbuy.R.id;
import com.john.groupbuy.R.layout;
import com.john.groupbuy.R.string;
import com.john.groupbuy.lib.http.GlobalKey;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends BaseActivity implements OnLoginListener {
    private LoginView mLoginView;
    private boolean isUser = true;
    public static IWXAPI api;
	
    public final static String WEIXINAPPID = "wx9d2279ea33cddad9";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("onCreate");
        setContentView(R.layout.login);
		api = WXAPIFactory.createWXAPI(this,WEIXINAPPID);
		
        enableBackBehavior();
        initView(savedInstanceState);
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		if(GroupBuyApplication.sIsUserLogin)
		{
			setResult(RESULT_OK);
			finish();
		}
	}

	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(GlobalKey.IS_USER_KEY, isUser);
    }

    private void initView(Bundle savedInstanceState) {
    	if(savedInstanceState == null){
    		isUser = getIntent().getBooleanExtra(GlobalKey.IS_USER_KEY, true);
    	}else{
    		isUser = savedInstanceState.getBoolean(GlobalKey.IS_USER_KEY,true);
    	}
        mLoginView = new LoginView(this, isUser);
        mLoginView.setActivity(this);
        mLoginView.setOnLoginSuccess(this);
        api.handleIntent(getIntent(),mLoginView);

        if (isUser) {
            setTitle(R.string.title_activity_login);
        } else {
            setTitle(R.string.login_merchant);
        }

        LinearLayout innerLayout = (LinearLayout) findViewById(R.id.innerLayout);
        innerLayout.addView(mLoginView);
    }

    @Override
    public void finish() {
        hideInputMethod();
        super.finish();
    }

    protected void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && this.getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == GlobalKey.REQUEST_TENCENT_BINDING) {
            mLoginView.verifyBindingResult();
        }
    }

    @Override
    public void loginSuccess() {
        Toast.makeText(WXEntryActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
        finishActivity();
    }

    @Override
    public void loginFailuer() {
        Toast.makeText(WXEntryActivity.this, getString(R.string.login_failure), Toast.LENGTH_SHORT).show();
        // finishActivity();
    }

    @Override
    public void bindingSuccess() {
        Toast.makeText(WXEntryActivity.this, getString(R.string.tencent_login_success), Toast.LENGTH_SHORT).show();
        finishActivity();
    }

    @Override
    public void bindingFailure() {
        Toast.makeText(WXEntryActivity.this, getString(R.string.tencent_login_failure), Toast.LENGTH_SHORT).show();
        // finishActivity();
    }

    protected void finishActivity() {
        setResult(RESULT_OK);
        System.out.println("finish");
        this.finish();
    }

}
