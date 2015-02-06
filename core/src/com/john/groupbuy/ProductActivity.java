package com.john.groupbuy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.*;

import com.groupbuy.yidaole.CouponActivity;
import com.groupbuy.yidaole.wxapi.WXEntryActivity;
import com.john.groupbuy.MyScrollView.OnRefreshListener;
import com.john.groupbuy.MyScrollView.OnScrollListener;
import com.john.groupbuy.adapter.ProductPreviewAdapter;
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.*;
import com.john.util.*;
import com.john.util.GetTimeUtil.TimeCompareResult;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.bean.StatusCode;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.media.*;

import org.apache.http.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductActivity extends BaseActivity implements OnClickListener,
        OnRefreshListener, OnScrollListener, OnGlobalLayoutListener {

    public static final String KEY_PRODUCT_ID = "key_product_id";
    final static int COUPON_EXPIRED = 0;
    final static int COUPON_AVAILABLE = 1;
    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    private ProductInfo productInfo = null;
    private AutoScrollViewPager previewPager;
    private ProductPreviewAdapter previewAdapter;
    private FrameLayout previewLayout;
    private PageIndicator pageIndicator;

    private TextView mBuyCount;
    private TextView mTimeRemaining;
    // private TextView mIsRefund;
    // private TextView mIsExpireRefund;
    private TextView mTitle;
    private TextView mSummary;
    private Button mDetailBtn;
    private TextView mSetMeal; // 套餐内容
    private TextView mNotice;
    private MyScrollView mScrollView;
    private RefreshTask mTask;
    private TextView partnerName;
    private TextView partnerAddress;
    private ActionBarHolder floatActionBar;
    private ActionBarHolder innerActionBar;
    private boolean isShowingFloatAction = false;
    private LinearLayout productLayout;
    private ProductTask productTask;
    private TextView statusLabel;
    private PageEntity pageEntity;
    private List<ProductInfo> productList;
    private boolean isLoading = false;
    private LinearLayout relationSection = null;
    private View sectionHints;
    private IWXAPI wxapi;
    private boolean supportWxCircle = false;
    private UMSocialService umengSdk;
    private ShareData shareData;
    private PopupWindow popupWindow;
    private LoadingView loadingView;
    

    private boolean initCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleAnalyticsManager.INSTANCE.onActivityCreate(ProductActivity.class);
        setContentView(R.layout.product_activity);
        productList = new ArrayList<ProductInfo>();

        initViewComponents();
        initShareSdk();
        enableBackBehavior();
        setTitle(R.string.product_detail);
    }

    private void initShareSdk() {
        // init wx Platform
        CacheManager manager = CacheManager.getInstance();
        if (manager.supportWeiXin()) {
            wxapi = WXAPIFactory.createWXAPI(this, manager.getWxAppid());
            int wxSdkVersion = wxapi.getWXAppSupportAPI();
            supportWxCircle = wxSdkVersion >= TIMELINE_SUPPORTED_VERSION;
        }
        // init umeng share sdk
        umengSdk = UMServiceFactory.getUMSocialService("com.umeng.share",
                RequestType.SOCIAL);
        // 添加新浪和QQ空间的SSO授权支持
//		SocializeConfig socializeConfig = umengSdk.getConfig();
        // socializeConfig.setSsoHandler(new SinaSsoHandler());
//		socializeConfig.setSsoHandler(new QZoneSsoHandler(this, "100424468",
//				"c7394704798a158208a74ab60104f0ba"));
        // 添加腾讯微博SSO支持
//		socializeConfig.setSsoHandler(new TencentWBSsoHandler());
    }

    private void stopTask() {
        if (productTask == null)
            return;
        productTask.cancel(true);
        productTask = null;
    }

    @Override
    protected void onDestroy() {
        stopTask();
        if (popupWindow != null)
            popupWindow.dismiss();
        GoogleAnalyticsManager.INSTANCE.dispatchLocalHits();
        super.onDestroy();
    }

    protected void initViewComponents() {
        mScrollView = (MyScrollView) findViewById(R.id.product_scroll_view);
        mScrollView.setScrollListener(this);
        mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        View rootView = findViewById(R.id.float_action_bar);
        floatActionBar = new ActionBarHolder();
        floatActionBar.inflate(rootView);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        LinearLayout mLinearLayout = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.inner_product_layout, null);
        mLinearLayout.setLayoutParams(params);
        mScrollView.addChild(mLinearLayout);

        mScrollView.setOnRefreshListener(this);

        previewLayout = (FrameLayout) findViewById(R.id.product_preview_layout);
        previewAdapter = new ProductPreviewAdapter(this);
        previewPager = (AutoScrollViewPager) findViewById(R.id.product_preview_pager);
        previewPager.setLoop(true);
        previewPager.setAdapter(previewAdapter);

        pageIndicator = (PageIndicator) findViewById(R.id.product_page_indicator);
        previewPager.setOnPageChangeListener(pageIndicator);

        mBuyCount = (TextView) findViewById(R.id.buy_count);
        mTimeRemaining = (TextView) findViewById(R.id.time_remaining);
        // mIsRefund = (TextView) findViewById(R.id.is_refund);
        // mIsExpireRefund = (TextView) findViewById(R.id.is_expire_refund);
        mTitle = (TextView) findViewById(R.id.title);
        mSummary = (TextView) findViewById(R.id.summary);
        mSetMeal = (TextView) findViewById(R.id.set_meal);

        int maxLines = CacheManager.getInstance().getDetailMaxLine();
        if (maxLines != 0)
            mSetMeal.setMaxLines(maxLines);

        mNotice = (TextView) findViewById(R.id.notice);

        mDetailBtn = (Button) findViewById(R.id.detail_btn);

        findViewById(R.id.partner_section).setOnClickListener(this);
        partnerName = (TextView) findViewById(R.id.partner_name);
        partnerAddress = (TextView) findViewById(R.id.partner_address);

        mDetailBtn.setOnClickListener(this);

        findViewById(R.id.phone_call_btn).setOnClickListener(this);

        rootView = findViewById(R.id.inner_action_bar);
        innerActionBar = new ActionBarHolder();
        innerActionBar.inflate(rootView);

        relationSection = (LinearLayout) findViewById(R.id.relation_coupon_section);
        productLayout = (LinearLayout) findViewById(R.id.product_layout);
        statusLabel = (TextView) findViewById(R.id.status_label);
        statusLabel.setOnClickListener(this);

        sectionHints = findViewById(R.id.section_hints);

        loadingView = (LoadingView) findViewById(R.id.product_loading_view);

    }

    protected void initComponentsData() {
        refreshComponentsData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void refreshComponentsData() {
        pageEntity = null;
        productList.clear();
        productLayout.removeAllViews();
        statusLabel.setText(R.string.loading_partner_coupon_hint);

        //TODO
        if (productInfo == null)
            productInfo = CacheManager.getInstance().getCurrentProduct();

        if (productInfo == null) {
            statusLabel.setVisibility(View.GONE);
            this.finish();
            return;
        }

        List<String> imageUrls = productInfo.getPreviewImages();
        if (imageUrls != null) {
            previewAdapter.setImageUrls(imageUrls);
            pageIndicator.setPageCount(imageUrls.size());
            previewPager.startScroll();
        }

        floatActionBar.setData(productInfo);
        innerActionBar.setData(productInfo);

        mBuyCount.setText(String.format(getString(R.string.person_count),
                productInfo.now_number));

        TimeCompareResult tc = GetTimeUtil
                .currentTimeCompareWith(productInfo.end_time);
        String text;

        boolean outOfDate = false;
        if (tc.remainingDays > 2L) {
            text = "剩余3天以上";
        } else {
            if (tc.remainingDays <= 0) {
                if (tc.remainingHours <= 0) {
                    if (tc.remainingMinute <= 0) {
                        text = "已过期";
                        outOfDate = true;
                    } else {
                        text = "只剩余" + tc.remainingMinute + "分钟";
                    }
                } else {
                    text = "剩余" + tc.remainingHours + "小时" + tc.remainingMinute
                            + "分钟";
                }
            } else {
                text = "剩余" + tc.remainingDays + "天" + tc.remainingHours + "小时"
                        + tc.remainingMinute + "分钟";
            }
        }

        mTimeRemaining.setText(text);

        if (outOfDate) {
            floatActionBar.rootView.findViewById(R.id.panic_buying)
                    .setVisibility(View.GONE);
            innerActionBar.rootView.findViewById(R.id.panic_buying)
                    .setVisibility(View.GONE);
        }

        mSummary.setText(productInfo.title);

        if (productInfo.partner == null) {
            findViewById(R.id.partner_layout).setVisibility(View.GONE);
        } else {
            findViewById(R.id.partner_layout).setVisibility(View.VISIBLE);
            PartnerInfo partner = productInfo.partner;
            if (!TextUtils.isEmpty(partner.getTitle()))
                partnerName.setText(partner.getTitle());
            if (!TextUtils.isEmpty(partner.getAddress()))
                partnerAddress.setText(partner.getAddress());
        }

        if (productInfo.partner != null) {
            mTitle.setText(productInfo.partner.getTitle());
        } else {
            mTitle.setText(productInfo.product);
        }

        if (!TextUtils.isEmpty(productInfo.summary)) {
            // mSetMeal.setText(Html.fromHtml(productInfo.summary));
            mSetMeal.setText(productInfo.summary);
        } else {
            mSetMeal.setText(mSummary.getText());
        }

        if (!TextUtils.isEmpty(productInfo.notice)) {
            // mNotice.setText(Html.fromHtml(productInfo.notice));
            mNotice.setText(productInfo.notice);
        } else {
            sectionHints.setVisibility(View.GONE);
        }
        getPartnerProduct();
        updateShareContent();
    }

    private void initShareContent(BaseShareContent content) {
        content.setShareContent(shareData.description);
        content.setTitle(shareData.title);
        content.setShareImage(new UMImage(this, shareData.thumb));
        content.setTargetUrl(shareData.url);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.weixin_share || v.getId() == R.id.weixin_circle_share) {
            WXWebpageObject webPage = new WXWebpageObject();
            webPage.webpageUrl = shareData.url;
            WXMediaMessage msg = new WXMediaMessage(webPage);
            msg.title = shareData.title;
            msg.description = shareData.description;
            Bitmap thumb = ImageLoader.getInstance().loadImageSync(shareData.thumb);
            msg.thumbData = WXUtil.bmpToByteArray(thumb, false);
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = buildTransaction("webpage");
            req.message = msg;
            if (v.getId() == R.id.weixin_circle_share) {
                req.scene = SendMessageToWX.Req.WXSceneTimeline;
            } else {
                req.scene = SendMessageToWX.Req.WXSceneSession;
            }
            wxapi.sendReq(req);
        } else if (v.getId() == R.id.qzone_share) {
            QZoneShareContent content = new QZoneShareContent();
            initShareContent(content);
            umengSdk.setShareMedia(content);
            umengSdk.postShare(this, SHARE_MEDIA.QZONE, new SnsPostListener() {
                @Override
                public void onStart() {
                    showToast(R.string.share_to_qzone_hint);
                }

                @Override
                public void onComplete(SHARE_MEDIA arg0, int arg1,
                                       SocializeEntity arg2) {
                    if (arg1 == StatusCode.ST_CODE_SUCCESSED) {
                        showToast(R.string.share_ok);
                    } else {
                        showToast(R.string.share_failure);
                    }
                }
            });

        } else if (v.getId() == R.id.tx_weibo_share) {
            TencentWbShareContent shareContent = new TencentWbShareContent();
            initShareContent(shareContent);
            umengSdk.setShareMedia(shareContent);
            umengSdk.postShare(this, SHARE_MEDIA.TENCENT, new SnsPostListener() {
                @Override
                public void onStart() {
                    showToast(R.string.share_to_tencent_weibo);
                }

                @Override
                public void onComplete(SHARE_MEDIA arg0, int arg1,
                                       SocializeEntity arg2) {
                    if (arg1 == StatusCode.ST_CODE_SUCCESSED) {
                        showToast(R.string.share_ok);
                    } else {
                        showToast(R.string.share_failure);
                    }
                }
            });
        } else if (v.getId() == R.id.sina_share) {
            SinaShareContent shareContent = new SinaShareContent();
            initShareContent(shareContent);
            umengSdk.setShareMedia(shareContent);
            umengSdk.postShare(this, SHARE_MEDIA.SINA, new SnsPostListener() {
                @Override
                public void onStart() {
                    showToast(R.string.share_to_sina);
                }

                @Override
                public void onComplete(SHARE_MEDIA arg0, int arg1,
                                       SocializeEntity arg2) {
                    if (arg1 == StatusCode.ST_CODE_SUCCESSED) {
                        showToast(R.string.share_ok);
                    } else {
                        showToast(R.string.share_failure);
                    }
                }
            });
        } else if (v.getId() == R.id.sms_share) {
            SmsShareContent shareContent = new SmsShareContent();
            shareContent.setShareContent(shareData.description);
            umengSdk.setShareMedia(shareContent);
            umengSdk.shareSms(this);
        } else if (v.getId() == R.id.detail_btn) {
            showDetails();
        } else if (v.getId() == R.id.phone_call_btn) {
            if (productInfo.partner == null) {
                return;
            }
            String phoneNum = productInfo.partner.getPhone();
            if (TextUtils.isEmpty(phoneNum))
                return;
            String url = String.format("tel:%s", phoneNum);
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            startActivity(intent);
        } else if (v.getId() == R.id.partner_section) {
            showPartnerInfomation();
        } else if (v.getId() == R.id.status_label) {
            if (pageEntity != null && !pageEntity.isLastPage()) {
                getPartnerProduct();
            }
        } else {
            ProductInfo info = (ProductInfo) v.getTag();
            if (info == null)
                return;
            CacheManager.getInstance().setCurrentProduct(info);
            startActivity(new Intent(this, ProductActivity.class));
        }
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private void getPartnerProduct() {
        if (productInfo == null || productInfo.partner == null) {
            relationSection.setVisibility(View.GONE);
            return;
        }
        if (isLoading) {
            Toast.makeText(this, R.string.loading_partner_coupon_hint,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int pageNum = 1;
        if (pageEntity != null)
            pageNum = pageEntity.getCurrentPage() + 1;
        String params = String.format(Locale.getDefault(),
                "Tuan/goodsList&page=%d&partnerid=%s", pageNum,
                productInfo.partner.getId());
        String url = Interface.DEFAULT_APP_HOST + params;
        productTask = new ProductTask();
        productTask.execute(url);

        isLoading = true;
    }

    protected void showPartnerInfomation() {
        if (productInfo == null || productInfo.partner == null)
            return;

        if (TextUtils.isEmpty(productInfo.partner.getImage()))
            productInfo.partner.setImage(productInfo.getSmallImageUrl());
        PartnerActivity.setPartnerInfo(productInfo.partner);
        Intent intent = new Intent(this, PartnerActivity.class);
        startActivity(intent);

    }

    protected void showDetails() {
        Intent intent = new Intent(ProductActivity.this, WebViewActivity.class);
        String detailsUrl = Interface.S_PRODUCE_DETAILES + productInfo.id;
        intent.putExtra(WebViewActivity.KEY_TITLE,
                getString(R.string.current_details));
        intent.putExtra(WebViewActivity.KEY_LOADING_URL, detailsUrl);
        this.startActivity(intent);
    }

    
    protected void submitProduct() {
        //TODO
        
        if (GroupBuyApplication.sIsUserLogin == false) {
            Intent intent = new Intent(this, WXEntryActivity.class);
            intent.putExtra(GlobalKey.IS_USER_KEY, true);
            this.startActivityForResult(intent, 1);
        } else {
        	buy();
        }
        
    	/*以前是支付页面，现在不要了
        if (GroupBuyApplication.sIsUserLogin == false) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(GlobalKey.IS_USER_KEY, true);
            this.startActivityForResult(intent, 1);
        } else {
            Intent intent = new Intent(this, SubmitOrderActivity.class);
            startActivityForResult(intent, GlobalKey.REQUEST_PAYMENT);
        }*/
    }
    
    private void buy()
    {
        String params = String.format(Locale.getDefault(),
                "Tuan/detail_new&id=%1$s&user_id=%2$s",
                productInfo.id, GroupBuyApplication.sUserInfo.id);
        String url = Interface.DEFAULT_APP_HOST + params;
        CouponTask couponTask = new CouponTask();
        couponTask.execute(url);
    }
    
    private ProgressDialog progressDialog;
    private final class CouponTask extends
    		AsyncTask<String,Void,SingleCouponInfo> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialog.show(ProductActivity.this, "", "购买中……");
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
		    	Intent intent = new Intent(ProductActivity.this,CouponActivity.class).putExtra("SingleCouponInfo", result);
		        startActivityForResult(intent, GlobalKey.REQUEST_PAYMENT);
		    }
		    else
		    {
		    	Toast.makeText(ProductActivity.this, "购买失败！", Toast.LENGTH_LONG).show();
		    }
//		    couponview.getEdaole_code().setText(result.couponcode);
		    //Log.i("调试分享码",result.couponcode);
		    
		}
		
	}

    private void showQRCode() {
        startActivity(new Intent(this, DisplayQRActivity.class));
    }

    protected void updateShareContent() {
        String title = mTitle.getText().toString();
        String summary = mSummary.getText().toString();
        String shareUrl = Interface.DNS_NAME + "/team.php?id=" + productInfo.id;
        if (getPackageName().contains("zhongtuanwang")) {
            //eg:http://www.tuanln.com/tuan.php?ctl=deal&id=2878
            shareUrl = "http://www.tuanln.com/tuan.php?ctl=deal&id=" + productInfo.id;
        }
        String shareString = title + summary + shareUrl;
        shareString = getString(R.string.format_share,
                getString(R.string.app_name), shareString);
        shareData = new ShareData();
        shareData.title = getString(R.string.excellent_title);
        shareData.description = shareString;
        shareData.thumb = productInfo.getSmallImageUrl();
        shareData.url = shareUrl;
    }

    protected void share() {
        if (shareData == null)
            return;
        View contentView = getLayoutInflater().inflate(R.layout.share_layout, null);
        popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        if (CacheManager.getInstance().supportWeiXin()) {
            TextView weixinShare = (TextView) contentView.findViewById(R.id.weixin_share);
            weixinShare.setClickable(true);
            weixinShare.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.umeng_socialize_wechat, 0, 0);
            weixinShare.setOnClickListener(this);
            if (supportWxCircle) {
                TextView wxCircle = (TextView) contentView.findViewById(R.id.weixin_circle_share);
                wxCircle.setClickable(true);
                wxCircle.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.umeng_socialize_wxcircle, 0, 0);
                wxCircle.setOnClickListener(this);
            }
        }

        contentView.findViewById(R.id.outside_button).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
        contentView.findViewById(R.id.qzone_share).setOnClickListener(this);
        contentView.findViewById(R.id.tx_weibo_share).setOnClickListener(this);
        contentView.findViewById(R.id.sina_share).setOnClickListener(this);
        contentView.findViewById(R.id.sms_share).setOnClickListener(this);
        View parentView = findViewById(android.R.id.content);
        popupWindow.setTouchable(true);
        popupWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
        popupWindow.update();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // UMSsoHandler ssoHandler =
        // umengSdk.getConfig().getSsoHandler(requestCode) ;
        // if(ssoHandler != null){
        // ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        // }

        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == 1) {
            // login success
//            Intent intent = new Intent(this, SubmitOrderActivity.class);
//            this.startActivity(intent);
        	buy();
        } else if (requestCode == GlobalKey.REQUEST_PAYMENT) {
            // 支付完成后关闭当前页面
//            this.finish();
        }
    }

    protected void OnReceiveData(String str) {
        mScrollView.onRefreshComplete();
    }

    @Override
    public void onRefresh() {
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
        mTask = new RefreshTask();
        mTask.execute(productInfo.id);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);

        if (productInfo != null)
            CacheManager.getInstance().setCurrentProduct(productInfo);

        if (initCompleted)
            return;

        initCompleted = true;
        XGPushClickedResult result = XGPushManager.onActivityStarted(this);
        if (result == null) {
            initComponentsData();
            return;
        }

        String id = CacheManager.getInstance().getCustomId();
        if (id == null) {
            finish();
            return;
        }

//        String customContent = result.getCustomContent();
//        if (customContent == null || customContent.length() == 0){
//            finish();
//            return;
//        }
//        Gson gson = new GsonBuilder().serializeNulls().create();
//        try {
//            XGCustomData customData = gson.fromJson(customContent,XGCustomData.class);
//            String id = customData.getId();
//            if (id == null || id.length() == 0){
//                finish();
//                return;
//            }
        mTask = new RefreshTask();
        mTask.setInit(true);
        mTask.execute(id);
        loadingView.setVisibility(View.VISIBLE);
        loadingView.showMessage(R.string.loading_product_hint, true);
//        }catch (JsonSyntaxException e){
//            e.printStackTrace();
//            finish();
//        }
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        XGPushManager.onActivityStoped(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            share();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollChanged() {
        View innerView = innerActionBar.rootView;
        View floatView = floatActionBar.rootView;
        int innerLocation[] = new int[2];
        int scrollViewLoaction[] = new int[2];
        innerView.getLocationInWindow(innerLocation);
        mScrollView.getLocationInWindow(scrollViewLoaction);
        boolean needShowFlag = innerLocation[1] < scrollViewLoaction[1];
        if (needShowFlag && !isShowingFloatAction) {
            isShowingFloatAction = true;
            floatView.setVisibility(View.VISIBLE);
        } else if (!needShowFlag && isShowingFloatAction) {
            isShowingFloatAction = false;
            floatView.setVisibility(View.INVISIBLE);
        }
        innerLocation = null;
        scrollViewLoaction = null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onGlobalLayout() {
        mScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        int width = previewLayout.getWidth();
        int height = (int) (width / 1.6f);
        LayoutParams params = previewLayout.getLayoutParams();
        params.width = width;
        params.height = height;
        previewLayout.requestLayout();
    }

    private void updateProductLayout() {
        if (productList.size() == 0 || pageEntity == null) {
            relationSection.setVisibility(View.GONE);
            statusLabel.setText(R.string.no_coupon);
            return;
        }
        relationSection.setVisibility(View.VISIBLE);
        if (pageEntity.isLastPage())
            statusLabel.setVisibility(View.GONE);
        else
            statusLabel.setText(R.string.state_normal);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        float density = dm.density;

        for (ProductInfo product : productList) {
            if (product.id.equalsIgnoreCase(productInfo.id))
                continue;
            /*
            View rootView = getLayoutInflater().inflate(
                    R.layout.partner_product_item, null);
            View rootView = getLayoutInflater().inflate(
                    R.layout.item_product, null);
            ViewHolder holder = new ViewHolder(rootView);
            holder.setData(product);
            LinearLayout.LayoutParams parmas = new LinearLayout.LayoutParams(0,
                    0);
            parmas.width = LinearLayout.LayoutParams.MATCH_PARENT;
            parmas.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            parmas.topMargin = (int) (5 * density);
            productLayout.addView(rootView, parmas);
            */
            View rootView = getLayoutInflater().inflate(
                    R.layout.item_product, null);
            ViewHolder holder = new ViewHolder(rootView);
            holder.setData(product);
            
          //新改设置透明度
    		holder.title.getBackground().setAlpha(150);	
    		//设置控件高度
    		int width = productLayout.getWidth();
    		int height = width*9/16;
    		LinearLayout.LayoutParams parmas = new LinearLayout.LayoutParams(0,
                    0);
    		parmas.width  = width;
    		parmas.height  = height;
    		
            productLayout.addView(rootView, parmas);
            
        }

        if (productLayout.getChildCount() == 0)
            relationSection.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
            return;
        }
        super.onBackPressed();
    }

    protected class DateInfo {
        public long days;
        public long hours;
        public long minute;
        public long second;
    }

    protected class RefreshTask extends
            AsyncTask<String, Void, SingleProductInfo> {

        private boolean init = false;

        public boolean isInit() {
            return init;
        }

        public void setInit(boolean init) {
            this.init = init;
        }

        @Override
        protected SingleProductInfo doInBackground(String... params) {
            SingleProductInfo info = null;
            try {
                info = FactoryCenter.getProcessCenter().refreshProductInfo(
                        params[0]);
            } catch (HttpResponseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return info;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(SingleProductInfo result) {
            super.onPostExecute(result);
            if (isInit()) {
                if (result == null || result.result == null) {
                    loadingView.showMessage(R.string.connecting_error, false);
                    return;
                }
                productInfo = result.result;
                CacheManager.getInstance().setCurrentProduct(productInfo);
                loadingView.setVisibility(View.GONE);
                refreshComponentsData();
                return;
            }

            if (result == null) {
                mScrollView.onRefreshComplete();
                statusLabel.setText(R.string.partner_coupon_fail_hint);
                Toast.makeText(ProductActivity.this, R.string.connecting_error,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (result == null || result.result.id == null) {
                Toast.makeText(ProductActivity.this,
                        getString(R.string.refresh_failure), Toast.LENGTH_SHORT)
                        .show();
            } else {
                productInfo = result.result;
                refreshComponentsData();
                Toast.makeText(ProductActivity.this,
                        getString(R.string.refresh_completed),
                        Toast.LENGTH_SHORT).show();
            }
            mScrollView.onRefreshComplete();
        }

    }

    private class ActionBarHolder {

        public TextView teamPrice;
        public DiscountView salePrice;
        public Button panicBuyBtn;
        public View rootView;

        public void inflate(View rootView) {
            this.rootView = rootView;
            teamPrice = (TextView) rootView.findViewById(R.id.group_buy_price);
            salePrice = (DiscountView) rootView.findViewById(R.id.sale_price);
            panicBuyBtn = (Button) rootView.findViewById(R.id.panic_buying);
            panicBuyBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitProduct();
                }
            });
        }

        public void setData(ProductInfo info) {
            teamPrice.setText(getString(R.string.format_team_buy,
                    info.getTeamPrice()));
            salePrice.setText(getString(R.string.format_sale_price,
                    info.getMarketPrice()));
        }

    }

    private final class ViewHolder {
        public TextView title;
        public ImageView icon;
        public TextView detail;
        public TextView price;
        public TextView marketPrice;
        public View maskView;

        ViewHolder(View rootView) {
            title = (TextView) rootView.findViewById(R.id.title_text_view);
            icon = (ImageView) rootView.findViewById(R.id.product_icon);
            detail = (TextView) rootView.findViewById(R.id.detail_text_view);
            price = (TextView) rootView.findViewById(R.id.price_text_view);
            marketPrice = (TextView) rootView
                    .findViewById(R.id.discount_text_view);
            maskView = (View) rootView.findViewById(R.id.mask_view);
            maskView.setOnClickListener(ProductActivity.this);
        }

        public void setData(ProductInfo item) {
            maskView.setTag(item);
            if (item.partner != null) {
                title.setText(item.partner.getTitle());
            } else {
                title.setText(item.product);
            }
            detail.setText(item.title);
            price.setText(getString(R.string.format_team_buy,
                    item.getTeamPrice()));
            marketPrice.setText(getString(R.string.format_sale_price,
                    item.getMarketPrice()));
            icon.setImageResource(R.drawable.default_pic_small);
            String imageUrl = item.getLargeImageUrl();
            ImageLoader.getInstance().displayImage(imageUrl, icon);
        }
    }

    private final class ProductTask extends
            AsyncTask<String, ProductListInfo, ProductListInfo> {

        @Override
        protected ProductListInfo doInBackground(String... params) {
            if (params.length != 1)
                return null;
            try {
                return HttpUtil.get(params[0], ProductListInfo.class);
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
        protected void onPostExecute(ProductListInfo result) {
            super.onPostExecute(result);
            isLoading = false;
            if (result == null || result.getResult() == null) {
                statusLabel.setText(R.string.partner_coupon_fail_hint);
                Toast.makeText(ProductActivity.this,
                        R.string.partner_coupon_fail_hint, Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            pageEntity = result.getResult().getPageEntity();
            productList = result.getResult().getProductList();
            updateProductLayout();
        }

    }

    private final class ShareData {
        String title;
        String thumb;
        String url;
        String description;
    }
}
