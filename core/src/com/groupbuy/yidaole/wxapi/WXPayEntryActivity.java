package com.groupbuy.yidaole.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.john.groupbuy.CacheManager;
import com.john.groupbuy.R;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
	
	private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";
    private IWXAPI api;
    private TextView status;
    private ImageView icon;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);
        initView();
        
    	api = WXAPIFactory.createWXAPI(this, CacheManager.getInstance().getWxConfig().getAppId());
    	
    	Intent intent = getIntent();
    	if(intent != null)
    		Log.e(TAG, intent.toString());
    	
        api.handleIntent(getIntent(), this);
    }

    private void initView(){
        status = (TextView) findViewById(R.id.wx_pay_result_text);
        icon = (ImageView) findViewById(R.id.wx_pay_result_icon);
        findViewById(R.id.wx_result_completed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WXPayEntryActivity.this.finish();
            }
        });
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            int errCode = resp.errCode;
            switch (errCode){
                case BaseResp.ErrCode.ERR_OK:
                    //payment success without do anything
                    break;
                default:
                    //failure
                    icon.setImageResource(R.drawable.ic_payment_fail);
                    status.setText(R.string.wx_payment_failure);
                    break;
            }
		}
	}
}