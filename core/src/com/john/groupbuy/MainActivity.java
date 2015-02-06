package com.john.groupbuy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
import com.john.groupbuy.lib.http.GlobalKey;
import com.john.util.CheckUpdateUtil;
import com.john.util.GoogleAnalyticsManager;
import com.tencent.android.tpush.XGPushManager;
import com.umeng.analytics.MobclickAgent;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends BaseActivity implements
        OnCheckedChangeListener, Callback {

    private final String DATA_SELECTED_ID = "data_selected_id";

    private final int RESET_EXIT_FLAG = 1;
    private RadioGroup radioGroup;
    private Handler mHandler = null;
    private boolean mExitAppFlag = false;
    private int selectedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onError(this);
        MobclickAgent.updateOnlineConfig(this);
        GoogleAnalyticsManager.INSTANCE.onActivityCreate(MainActivity.class);

        setContentView(R.layout.fragment_main);
        initComponent();
        if (savedInstanceState == null)
            onCheckedChanged(radioGroup, R.id.main_recommand);
        else {
            int id = savedInstanceState.getInt(DATA_SELECTED_ID,
                    R.id.main_recommand);
            onCheckedChanged(radioGroup, id);
        }

        //XGPushConfig.enableDebug(this,true);
        if (CacheManager.getInstance().isSupportPush())
            XGPushManager.registerPush(this);

        GroupBuyApplication.getInstance().registerReceiver();
        checkUpdate();

        try {
            JSONObject props = new JSONObject();
            props.put("Gender", "Female");
            props.put("Plan", "Premium");
            CacheManager.getInstance().getMixpanelAPI().track("MainActivity created",props);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkUpdate() {
        if (CacheManager.getInstance().isNeedUpdate()) {
            SharedPreferences sp = getSharedPreferences(
                    GlobalKey.SHARE_PREFERS_NAME, Context.MODE_PRIVATE);
            boolean needCheckUpdate = sp.getBoolean(GlobalKey.CHECK_UPDATE_KEY,
                    true);
            if (needCheckUpdate)
                CheckUpdateUtil.getInstance().CheckUpdate(this, true);
            CacheManager.getInstance().setNeedUpdate(false);
        }
    }

    private void initComponent() {
        radioGroup = (RadioGroup) findViewById(R.id.layout_navi);
        radioGroup.setOnCheckedChangeListener(this);

        TypedArray array = getResources().obtainTypedArray(R.array.NormalDrawableList);
        if (array == null)
            return;
        for (int i = 0; i < radioGroup.getChildCount(); ++i) {
            View child = radioGroup.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) child;
                Drawable drawables[] = radioButton.getCompoundDrawables();
                if (drawables == null || drawables.length == 0)
                    continue;
                Drawable drawableTop = array.getDrawable(i);
                if (drawableTop == null)
                    continue;
                int width = drawableTop.getIntrinsicWidth();
                int height = drawableTop.getIntrinsicHeight();
                drawableTop.setBounds(0,0,width,height);
                radioButton.setCompoundDrawables(null,drawableTop,null,null);
            }
        }
        array.recycle();
    }

    private void hideAllFragment(FragmentManager manager) {
        List<Fragment> fragmentList = manager.getFragments();
        if (fragmentList == null || fragmentList.isEmpty())
            return;
        FragmentTransaction transaction = manager.beginTransaction();
        for (Fragment fragment : fragmentList) {
            transaction.hide(fragment);
        }
        transaction.commit();
    }

    private void switchFragment(Class<? extends Fragment> type, Bundle args) {
        FragmentManager manager = getSupportFragmentManager();
        String tagString = type.getSimpleName();
        Fragment fragment = manager.findFragmentByTag(tagString);
        hideAllFragment(manager);
        if (fragment == null) {
            try {
                fragment = type.newInstance();
                if (args != null)
                    fragment.setArguments(args);
                FragmentTransaction ts = manager.beginTransaction();
                ts.add(R.id.layout_body, fragment, tagString);
                ts.commit();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            FragmentTransaction ts = manager.beginTransaction();
            ts.show(fragment);
            ts.commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DATA_SELECTED_ID, selectedId);
    }

    @Override
    public void onBackPressed() {
        if (mExitAppFlag) {
            CacheManager.getInstance().reset();
            super.finish();
        } else {
            Toast.makeText(this, getString(R.string.exit_app_once_more),
                    Toast.LENGTH_SHORT).show();
            mExitAppFlag = true;
            if (mHandler == null)
                mHandler = new Handler(this);
            mHandler.sendEmptyMessageDelayed(RESET_EXIT_FLAG, 3000L);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    protected void onDestroy() {
        GroupBuyApplication.getInstance().onDestoryApplication();
        GoogleAnalyticsManager.INSTANCE.dispatchLocalHits();
        CacheManager.getInstance().getMixpanelAPI().flush();
        super.onDestroy();
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == RESET_EXIT_FLAG) {
            mExitAppFlag = false;
            return true;
        }
        return false;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (selectedId == checkedId)
            return;
        selectedId = checkedId;
        if (checkedId == R.id.main_recommand) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(ProductListFragment.ARG_DISPLAY_CATEGORY, true);
            switchFragment(ProductListFragment.class, bundle);
        } else if (checkedId == R.id.main_coupon) {
            switchFragment(UserHomeFragment.class, null);
        } else if (checkedId == R.id.main_setting) {
            switchFragment(SettingFragment.class, null);
        } else if (checkedId == R.id.main_near_by) {
            switchFragment(PartnerNearbyFragment.class, null);
        }
    }
}

