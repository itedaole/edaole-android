package com.john.groupbuy;

import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import com.john.groupbuy.lib.http.UserInfo;
import com.john.util.BaiduLocationManager;
import com.john.util.CheckUpdateUtil;
import com.john.util.GoogleAnalyticsManager;
import com.john.util.GoogleLocationManager;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class GroupBuyApplication extends Application {
    public static boolean sIsUserLogin = false; //用户是否登陆
    public static boolean sIsPartnerLogin = false; //商户是否登陆
    public static String sBindingPhone;
    public static UserInfo sUserInfo = null;
    private static GroupBuyApplication sInstance = null;
    private boolean isRegistered = false;
    final private String TOKEN_ID = "fa15492aecc965e3fd8626d3ccb4adfc";

    public GroupBuyApplication() {
        sInstance = this;
    }

    public static GroupBuyApplication getInstance() {
        return sInstance;
    }

    public void registerReceiver() {
        if (isRegistered)
            return;

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(CheckUpdateUtil.getInstance(), filter);
    }

    private void parseApplicationMetadata(Context context) {
        try {
            CacheManager manager = CacheManager.getInstance();
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            manager.setVersionName(pi.versionName);
            manager.setVersionCode(pi.versionCode);

            ApplicationInfo appInfo = getPackageManager()
                    .getApplicationInfo(getPackageName(),
                            PackageManager.GET_META_DATA);
            boolean zuitu_model = appInfo.metaData.getBoolean("ZUITU_MODEL", true);
            manager.setZuitu(zuitu_model);

            boolean useGoogleMap = appInfo.metaData.getBoolean("USE_GOOGLE_MAP",false);
            manager.setUseGoogleMap(useGoogleMap);

            boolean support_un = appInfo.metaData.getBoolean("SUPPORT_UN", false);
            manager.setSupportUN(support_un);

            int maxLine = appInfo.metaData.getInt("DETAIL_MAX_LINE", 0);
            manager.setDetailMaxLine(maxLine);

            String wxAppid = appInfo.metaData.getString("WEIXIN_APPID");
            manager.setWxAppId(wxAppid);

            int accessId = appInfo.metaData.getInt("XG_V2_ACCESS_ID",-1);
            String accessKey = appInfo.metaData.getString("XG_V2_ACCESS_KEY");
            if (accessId != -1 && !TextUtils.isEmpty(accessKey))
                CacheManager.getInstance().setSupportPush(true);

        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
    }

    public void onDestoryApplication() {
        if (isRegistered) {
            isRegistered = false;
            unregisterReceiver(CheckUpdateUtil.getInstance());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.default_pic_small)
                .showImageOnFail(R.drawable.default_pic_small)
                .showImageForEmptyUri(R.drawable.default_pic_small)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(options)
                .build();
        ImageLoader.getInstance().init(config);
        parseApplicationMetadata(this);

        if (CacheManager.getInstance().isUseGoogleMap()){
            GoogleLocationManager.INSTANCE.initManager(this);
        }else{
            BaiduLocationManager.getInstance().init(this);
        }

        GoogleAnalyticsManager.INSTANCE.init(this);
        CacheManager.getInstance().setMixpanelAPI(MixpanelAPI.getInstance(this,TOKEN_ID));
    }

}