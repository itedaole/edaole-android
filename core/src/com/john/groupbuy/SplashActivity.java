package com.john.groupbuy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import com.john.groupbuy.lib.http.GlobalKey;
import com.umeng.analytics.MobclickAgent;

public class SplashActivity extends Activity implements Callback{
    private Handler mHander ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHander = new Handler(this);
        setContentView(R.layout.activity_splash);
        mHander.sendEmptyMessageDelayed(1, 2000);
    }
    
    public void onResume() {
        super.onResume();
        com.umeng.common.Log.LOG = true;
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

	@Override
	public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case 1:
    		SharedPreferences sharedPref = getSharedPreferences(
    				GlobalKey.SHARE_PREFERS_NAME, Context.MODE_PRIVATE);
    		boolean firstLaunch = sharedPref.getBoolean(GlobalKey.FIRSET_LAUNCH, true);
    		if(firstLaunch){
    			Editor editor = sharedPref.edit();
    			editor.putBoolean(GlobalKey.FIRSET_LAUNCH, false);
    			editor.commit();
    			Intent intent = new Intent(this,GuideActivity.class);
    			startActivity(intent);
    		}else{
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
    		}
            finish();
            break;
        }
		return true;
	}

}
