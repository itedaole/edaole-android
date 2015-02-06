package com.john.groupbuy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.groupbuy.yidaole.wxapi.WXEntryActivity;
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.GlobalKey;
import com.john.groupbuy.lib.http.Interface;
import com.john.groupbuy.lib.http.StatusInfo;
import com.john.util.CheckUpdateUtil;
import com.john.util.HttpResponseException;
import com.john.util.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.IOException;

public class SettingFragment extends Fragment implements OnClickListener {

    private Button mLogin;
    private Button mVerifyBtn;
    private View mSpace;
    private View mSpace2;
    private Button mLogout;
    private ProgressDialog myDialog;
    private AsyncTask<String, Void, StatusInfo> mTask;

    private String mServiceNumber = null;
    private String mJoinUsNumber = null;

    private CleanCacheTask taskClean;

    private Button helpBtn;
    private Button checkUpdate;
    
    private CheckUpdateUtil updateUtil ;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.title_more);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
        	getActivity().setTitle(R.string.title_more);
        	BaseActivity activity = (BaseActivity) getActivity();
			activity.getSupportActionBar().setDisplayShowCustomEnabled(false);			        	
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	BaseActivity activity = (BaseActivity) getActivity();
		activity.getSupportActionBar().setDisplayShowCustomEnabled(false);
    }
    
    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_more, null);
        initView(rootView);
        updateUtil = CheckUpdateUtil.getInstance();
        return rootView;
    }

    private void initView(View rootView) {

        Button versionBtn = (Button) rootView.findViewById(R.id.versionNameBtn);
        versionBtn.setText(String.format(getString(R.string.version_name),
                CacheManager.getInstance().getVersionName()));
        mLogin = (Button) rootView.findViewById(R.id.login_btn);
        mLogin.setOnClickListener(this);

        mLogout = (Button) rootView.findViewById(R.id.logout_btn);
        mLogout.setOnClickListener(this);
        mVerifyBtn = (Button) rootView.findViewById(R.id.verify_ticket);
        mVerifyBtn.setOnClickListener(this);

        rootView.findViewById(R.id.cleanCache).setOnClickListener(this);

        mSpace = rootView.findViewById(R.id.space);
        mSpace2 = rootView.findViewById(R.id.space2);

        Button homePageBtn = (Button) rootView.findViewById(R.id.homepageBtn);
        homePageBtn.setText(String.format(getString(R.string.official_website), Interface.DNS_NAME));
        homePageBtn.setOnClickListener(this);

        Button weiboBtn = (Button) rootView.findViewById(R.id.weiboBtn);
        weiboBtn.setText(String.format(getString(R.string.weibo), Interface.S_WEIBO_URL));
        weiboBtn.setOnClickListener(this);

        Button serviceButton = (Button) rootView.findViewById(R.id.callServerTel);
        serviceButton.setText(getString(R.string.service_phone_text) + getString(R.string.service_phone));
        serviceButton.setOnClickListener(this);

        if (getString(R.string.service_phone).length() > 5) {
            mServiceNumber = getString(R.string.service_phone);
            mServiceNumber = mServiceNumber.replace("-", "");
        }

        Button joinUsButton = (Button) rootView.findViewById(R.id.callJoinUsTel);
        joinUsButton.setText(getString(R.string.join_us_text) + getString(R.string.join_us));
        joinUsButton.setOnClickListener(this);

        if (getString(R.string.join_us).length() > 5) {
            mJoinUsNumber = getString(R.string.join_us);
            mJoinUsNumber = mJoinUsNumber.replace("-", "");
        }
        
        helpBtn = (Button) rootView.findViewById(R.id.help);
        helpBtn.setOnClickListener(this);
        
        checkUpdate = (Button) rootView.findViewById(R.id.check_update);
        checkUpdate.setOnClickListener(this);

        rootView.findViewById(R.id.mu_support).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login_btn) {
            Intent intent = new Intent(getActivity(), WXEntryActivity.class);
            intent.putExtra(GlobalKey.IS_USER_KEY, false);
            startActivityForResult(intent, 1);
        } else if (v.getId() == R.id.verify_ticket) {
            Intent intent = new Intent(getActivity(), VerifyCouponActivity.class);
            this.startActivity(intent);
        } else if (v.getId() == R.id.logout_btn) {
            mTask = new LogoutTask().execute("");
        } else if (v.getId() == R.id.homepageBtn) {
            String url = Interface.DNS_NAME;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } else if (v.getId() == R.id.weiboBtn) {
            String url = Interface.S_WEIBO_URL;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } else if (v.getId() == R.id.callServerTel) {
            if (mServiceNumber == null)
                return;
            String url = String.format("tel:%s", mServiceNumber);
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            startActivity(intent);
        } else if (v.getId() == R.id.callJoinUsTel) {
            if (mJoinUsNumber == null)
                return;
            String url = String.format("tel:%s", mJoinUsNumber);
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            startActivity(intent);
        } else if (v.getId() == R.id.cleanCache) {
            clearCaches();
        } else if(v.getId() == R.id.help){
            showHelp();
        }else if(v.getId() == R.id.check_update){
            checkShouldUpdate();
        }else if (v.getId() == R.id.mu_support){
            startActivity(new Intent(getActivity(),AboutActivity.class));
        }
    }
    
    private void checkShouldUpdate(){
        updateUtil.CheckUpdate(getActivity(),false);
    }
    
    private void showHelp() {
        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        String helpUrl = Interface.DEFAULT_APP_HOST + "Pages/help";
        intent.putExtra(WebViewActivity.KEY_TITLE, getString(R.string.help));
        intent.putExtra(WebViewActivity.KEY_LOADING_URL, helpUrl);
        this.startActivity(intent);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(getActivity());
        updateUi();
    }

    protected void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    protected void clearCaches() {
        if (taskClean != null && taskClean.getStatus() != Status.FINISHED)
            taskClean.cancel(true);
        taskClean = new CleanCacheTask();
        taskClean.execute();
    }

    protected void updateUi() {
        if (GroupBuyApplication.sIsPartnerLogin == true) {
            mLogin.setVisibility(View.GONE);
            mLogout.setVisibility(View.VISIBLE);
            mSpace.setVisibility(View.VISIBLE);
            mSpace2.setVisibility(View.VISIBLE);
            mVerifyBtn.setVisibility(View.VISIBLE);
        } else {
            mLogin.setVisibility(View.VISIBLE);
            mLogout.setVisibility(View.GONE);
            mSpace.setVisibility(View.GONE);
            mSpace2.setVisibility(View.GONE);
            mVerifyBtn.setVisibility(View.GONE);
        }
    }

    public class CleanCacheTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            myDialog = new ProgressDialog(getActivity());
            myDialog.setCancelable(false);
            myDialog.setMessage(getString(R.string.clean_cache_tips));
            myDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ImageLoader.getInstance().clearDiscCache();
            ImageLoader.getInstance().clearMemoryCache();
            File dir = StorageUtils.getCacheDirectory(getActivity());
            deleteRecursive(dir);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            myDialog.dismiss();
            myDialog = null;
            Toast.makeText(getActivity(), R.string.clean_cache_ok_tips, Toast.LENGTH_LONG).show();
        }

    }

    protected class LogoutTask extends AsyncTask<String, Void, StatusInfo> {
        LogoutTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            myDialog = new ProgressDialog(getActivity());
            myDialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    mTask.cancel(true);
                }
            });
            myDialog.setIndeterminate(true);
            myDialog.setMessage(getString(R.string.partner_logout_process));

            myDialog.setCancelable(true);
            myDialog.show();

        }

        @Override
        protected StatusInfo doInBackground(String... params) {
            StatusInfo statusInfo = null;
            try {
                statusInfo = FactoryCenter.getUserInfoCenter().partnerLogout();

            } catch (HttpResponseException e) {
                LogUtil.warn(e.getMessage(), e);
            } catch (IOException e) {
                LogUtil.warn(e.getMessage(), e);
            }
            return statusInfo;
        }

        @Override
        protected void onPostExecute(StatusInfo result) {
            super.onPostExecute(result);

            myDialog.dismiss();
            
            if(result == null){
            	Toast.makeText(getActivity(), getString(R.string.connecting_error), Toast.LENGTH_SHORT).show();
            	return;
            }
            if (result.getStatus().equalsIgnoreCase("1")) {
                // 注销成功
                GroupBuyApplication.sIsPartnerLogin = false;
                updateUi();
            } else {
                Toast.makeText(getActivity(), getString(R.string.partner_logout_failure), Toast.LENGTH_SHORT).show();
            }
        }
    }
    

}
