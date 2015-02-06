package com.john.groupbuy;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.groupbuy.yidaole.wxapi.BaseTask;
import com.groupbuy.yidaole.wxapi.WXPreparePayTask;
import com.groupbuy.yidaole.wxapi.WXToken;
import com.groupbuy.yidaole.wxapi.WXTokenTask;
import com.john.groupbuy.lib.http.WXConfig;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for WX payment
 * Created by lucky on 14-8-5.
 */
public class WXPaymentActivity extends BaseActivity implements Handler.Callback {

    public static final int UNSUPPORTED_WX_PAYMENT = -100;
    public static final int REQUEST_CODE = 100;

    private final static int ID_ACCESS_TOKEN = 1;
    private final static int ID_PREPARE_PAY = 2;


    private IWXAPI wxapi;
    private Handler handler = new Handler(this);
    private TextView statusLabel;
    private ProgressBar loading;

    private ExecutorService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wx_pay_activity);
        enableBackBehavior();
        setTitle(R.string.wxpay);
        initComponents();
        initWXSdk();
    }

    private void initComponents() {
        statusLabel = (TextView) findViewById(R.id.wx_status_label);
        loading = (ProgressBar) findViewById(R.id.wx_pay_pb);
    }

    private void showResult(int resId) {
        loading.setVisibility(View.GONE);
        statusLabel.setText(resId);
    }

    private void initWXSdk() {
        CacheManager cacheManager = CacheManager.getInstance();
        String wxAppId = null;
        WXConfig wxConfig = cacheManager.getWxConfig();
        if (wxConfig != null)
            wxAppId = wxConfig.getAppId();
        else
            wxAppId = cacheManager.getWxAppid();

        if (wxAppId == null || wxAppId.length() == 0) {
            finish();
            return;
        }

        wxapi = WXAPIFactory.createWXAPI(this, wxAppId);
        wxapi.registerApp(wxAppId);

        if (!isSupportWXPay()) {
            setResult(UNSUPPORTED_WX_PAYMENT);
            finish();
            return;
        }

        service = Executors.newFixedThreadPool(5);
        WXTokenTask task = new WXTokenTask(ID_ACCESS_TOKEN, handler);
        service.submit(task);
    }

    private boolean isSupportWXPay() {
        return wxapi.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
    }

    @Override
    protected void onDestroy() {
        if (wxapi != null)
            wxapi.unregisterApp();
        if (service != null)
            service.shutdownNow();
        super.onDestroy();
    }

    @Override
    public boolean handleMessage(Message msg) {
        int taskId = msg.arg1;
        int code = msg.arg2;
        Object object = msg.obj;
        switch (taskId) {
            case ID_ACCESS_TOKEN:
                handleAccessToken(code, object);
                break;
            case ID_PREPARE_PAY:
                handlePreparePay(code, object);
                break;
            default:
                break;
        }
        return true;
    }

    private void handleAccessToken(int code, Object object) {
        if (code == BaseTask.CODE_FAILURE || object == null
                || !(object instanceof WXToken)) {
            showResult(R.string.wx_token_error);
            return;
        }
        WXToken wxToken = (WXToken) object;
        String token = wxToken.getAccess_token();
        if (token == null || token.length() == 0) {
            showResult(R.string.wx_token_error);
            return;
        }

        WXPreparePayTask task = new WXPreparePayTask(ID_PREPARE_PAY, handler);
        task.setAccessToken(token);
        service.submit(task);
    }

    private void handlePreparePay(int code, Object object) {
        if (code == BaseTask.CODE_FAILURE || object == null
                || !(object instanceof PayReq)) {
            showResult(R.string.prepare_id_error_hint);
            return;
        }

        PayReq payReq = (PayReq) object;
        wxapi.sendReq(payReq);

        showToast(R.string.wx_init_completed);
        finish();
    }


}
