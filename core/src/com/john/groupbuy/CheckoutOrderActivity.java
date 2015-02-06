package com.john.groupbuy;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.os.Handler.Callback;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.alipay.android.AlixId;
import com.alipay.android.BaseHelper;
import com.alipay.android.MobileSecurePayHelper;
import com.alipay.android.MobileSecurePayer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.hxcr.umspay.activity.Initialize;
import com.hxcr.umspay.util.Utils;
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.*;
import com.john.util.HttpResponseException;
import com.john.util.LogUtil;
import com.john.util.WXInterface;
import com.john.util.XmlUtil;
import com.john.util.XmlUtil.ParseException;
import com.tenpay.android.service.TenpayServiceHelper;
import com.umeng.analytics.MobclickAgent;
import com.unionpay.UPPayAssistEx;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CheckoutOrderActivity extends BaseActivity implements OnClickListener, Callback {
    final public static String READ_FROM_ORDERINFO_FLAG = "READ_FROM_ORDERINFO_FLAG";
    final protected static String BARGAINOR_ID_KEY = "bargainor_id=";
    final protected static String CANCEL_CODE = "660200003";
    final protected static String SUCCESS_CODE = "0";
    final static int MSG_PAY_RESULT = 100;
    final static int START_DOWNLOAD = 101;
    final static int UPDATE_PROGRESS = 102;
    final static int DOWNLOAD_FINISH = 103;
    final static int DOWNLOAD_ERROR = 104;
    final static String UNIONPAY_SUCCESS = "0000";
    final static String UNIONPAY_FAILURE = "1111";
    final static String UNIONPAY_RETRY = "2222";
    private static final int PLUGIN_NOT_INSTALLED = -1;
    private static final int PLUGIN_NEED_UPGRADE = 2;
    final private String DATA_SUBMIT_ORDER = "data_submit_order";
    // 接收支付返回值的Handler
    protected Handler mHandler;
    private SubmitOrderData mData;
    private ProgressDialog mDialog;
    private AsyncTask<String, Void, PaymentResult> mTask;
    private String mBargainorId;
    private RadioGroup mRadioGroup;
    private String mProductName;
    private ProgressDialog mpDialog;
    private long downLoadFileSize, fileSize;
    private String downloadPath;
    private boolean unionPaymented = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout_order);
        setTitle(R.string.title_CheckoutOrderActivity);
        enableBackBehavior();
        init(savedInstanceState);
    }

    // 安装apk方法
    private void installApk(String filename) {
        File file = new File(filename);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(Uri.fromFile(file), type);
        startActivity(intent);
        if (mpDialog != null) {
            mpDialog.cancel();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(DATA_SUBMIT_ORDER, mData);
        outState.putString(GlobalKey.PRODUCT_NAME_KEY, mProductName);
    }

    protected void init(Bundle savedInstanceState) {

        mHandler = new Handler(this);

        if (savedInstanceState == null && getIntent() != null) {
            mData = getIntent().getParcelableExtra(GlobalKey.PARCELABLE_KEY);
            mProductName = getIntent().getStringExtra(GlobalKey.PRODUCT_NAME_KEY);
        } else {
            mData = savedInstanceState.getParcelable(DATA_SUBMIT_ORDER);
            mProductName = savedInstanceState.getString(GlobalKey.PRODUCT_NAME_KEY);
        }

        if (mData == null)
            return;

        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        RadioButton mTenctPayment = (RadioButton) mRadioGroup.findViewById(R.id.tencent_pay);
        RadioButton mAlipayment = (RadioButton) mRadioGroup.findViewById(R.id.alipay);
        RadioButton mUnionPayment = (RadioButton) mRadioGroup.findViewById(R.id.unionPay);
        RadioButton mBalancePayment = (RadioButton) mRadioGroup.findViewById(R.id.balanceCheckBtn);
        RadioButton wxPayment = (RadioButton) mRadioGroup.findViewById(R.id.wx_pay);
        RadioButton unPayment = (RadioButton) mRadioGroup.findViewById(R.id.un_pay);
        RadioButton cashPayment = (RadioButton) mRadioGroup.findViewById(R.id.cash_pay);

        boolean isChecked = false;
        if (mData.pay_url != null) {
            mBalancePayment.setVisibility(View.VISIBLE);
            mBalancePayment.setChecked(true);
        } else {

            if (mData.upmp_pay != null && CacheManager.getInstance().isSupportUN()) {
                unPayment.setVisibility(View.VISIBLE);
                if (!isChecked) {
                    unPayment.setChecked(true);
                    isChecked = true;
                }
            }

            if (mData.alipay_str != null) {
                mAlipayment.setVisibility(View.VISIBLE);
                if (!isChecked) {
                    mAlipayment.setChecked(true);
                    isChecked = true;
                }
            }

            if (mData.wx_pay != null) {
                WXConfig wxConfig = WXConfig.fromJson(mData.wx_pay);
                if (wxConfig != null) {
                    wxPayment.setVisibility(View.VISIBLE);
                    CacheManager.getInstance().setWxConfig(wxConfig);
                    if (!isChecked) {
                        mTenctPayment.setChecked(true);
                        isChecked = true;
                    }
                }
            }

            if (mData.token_url != null) {
                mTenctPayment.setVisibility(View.VISIBLE);
                if (!isChecked) {
                    mTenctPayment.setChecked(true);
                    isChecked = true;
                }
            }

            if (mData.umspay_str != null) {
                mUnionPayment.setVisibility(View.VISIBLE);
                if (!isChecked) {
                    mUnionPayment.setChecked(true);
                    isChecked = true;
                }
            }
        }

        String packageName = getPackageName();
        TextView view = (TextView) findViewById(R.id.priceTextView);
        view.setText(mData.price);
        view = (TextView) findViewById(R.id.productName);
        view.setText(mProductName);
        view = (TextView) findViewById(R.id.goodsCountTextView);
        view.setText(mData.quantity);
        // 总价
        view = (TextView) findViewById(R.id.totalPriceTextView);
        view.setText(String.format("%.2f", mData.origin));
        // 余额
        view = (TextView) findViewById(R.id.moneyTextView);
        // view.setText(String.format("%.2f", mData.credit));
        view.setText(mData.money);
        // 还需支付
        float value = mData.origin - mData.credit;
        if (value < 0f) {
            value = 0f;
        }
        view = (TextView) findViewById(R.id.paymentTextView);
        view.setText(String.format("%.2f", value));

        Button button = (Button) findViewById(R.id.paymentButton);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.paymentButton) {
            onPaymentButtonClicked();
        }
    }

    protected void paymentWithTencent(String tokenId) {
        // 先得到财付通订单号

        if (tokenId == null || tokenId.length() < 32) {
            Toast.makeText(CheckoutOrderActivity.this, getString(R.string.checkout_failure), Toast.LENGTH_SHORT).show();
            return;
        }

        TenpayServiceHelper tenpayHelper = new TenpayServiceHelper(CheckoutOrderActivity.this);
        tenpayHelper.setLogEnabled(true); // 打开log 方便debug, 发布时不需要打开。
        // 判断并安装财付通安全支付服务应用
        // 走财付通支付流程
        TenpayServiceHelper helper = new TenpayServiceHelper(this);

        if (!helper.isTenpayServiceInstalled()) {
            helper.installTenpayService(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    Toast.makeText(CheckoutOrderActivity.this, "您取消了安装，不能完成支付", Toast.LENGTH_LONG).show();
                }
            });
        }

        // 构造支付参数
        HashMap<String, String> payInfo = new HashMap<String, String>();
        payInfo.put("token_id", tokenId); // 财付通订单号token_id
        payInfo.put(BARGAINOR_ID_KEY, mBargainorId); // 财付通合作商户ID,此为演示示例

        // 去支付
        tenpayHelper.pay(payInfo, mHandler, MSG_PAY_RESULT);
    }

    protected void onPaymentButtonClicked() {
        if (mRadioGroup.getCheckedRadioButtonId() == R.id.cash_pay) {
            View view = getLayoutInflater().inflate(R.layout.call_dialog, null);
            final Dialog dialog = new Dialog(this);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    0, 0);
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int width = (int) (dm.widthPixels - 20 * dm.density);
            params.width = width;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.setCancelable(true);
            dialog.setContentView(view, params);
            dialog.setTitle(R.string.prompt);
            view.findViewById(R.id.call_button).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    String url = String.format("tel:%s", "02474111222");
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(intent);
                }
            });
            view.findViewById(R.id.cancel_button).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            return;
        }
        if (mData != null && mData.pay_url != null && mData.pay_url.length() != 0) {
            mTask = new PaymentResultTask().execute(mData.pay_url);
            return;
        }
        if (mRadioGroup.getCheckedRadioButtonId() == R.id.tencent_pay) {
            if (mData.token_url != null && mData.token_url.length() > 10) {
                new GetTokenIdTask().execute(mData.token_url);
            } else {
                Toast.makeText(this, getString(R.string.checkout_failure), Toast.LENGTH_SHORT).show();
            }
        } else if (mRadioGroup.getCheckedRadioButtonId() == R.id.alipay) {
            if (mData.alipay_str != null && mData.alipay_str.length() > 10) {
                paymentWithAlipay();
            } else {
                Toast.makeText(this, getString(R.string.checkout_failure), Toast.LENGTH_SHORT).show();
            }
        } else if (mRadioGroup.getCheckedRadioButtonId() == R.id.unionPay) {
            if (mData.umspay_str != null && mData.umspay_str.length() > 10) {
                paymentWithUnionSdk();
            } else {
                Toast.makeText(this, getString(R.string.checkout_failure), Toast.LENGTH_SHORT).show();
            }
        } else if (mRadioGroup.getCheckedRadioButtonId() == R.id.wx_pay) {
            if (mData.wx_pay != null && mData.wx_pay.length() > 10) {
                paymentWithWX();
            } else {
                Toast.makeText(this, getString(R.string.checkout_failure), Toast.LENGTH_SHORT).show();
            }
        } else if (mRadioGroup.getCheckedRadioButtonId() == R.id.un_pay) {
            if (mData.upmp_pay != null && mData.upmp_pay.length() > 10) {
                paymentWithChinaUnion();
            } else {
                Toast.makeText(this, getString(R.string.checkout_failure), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void paymentWithChinaUnion() {
        Gson gson = new GsonBuilder().serializeNulls().create();
        try {
            ChinaUnion chinaUnion = gson.fromJson(mData.upmp_pay, ChinaUnion.class);
            String tn = chinaUnion.getTn();
            if (tn == null || tn.isEmpty()) {
                showToast(R.string.un_payment_failure);
                return;
            }
            int result = UPPayAssistEx.startPay(this, null, null, tn, "00");
            if (result == PLUGIN_NEED_UPGRADE || result == PLUGIN_NOT_INSTALLED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示");
                builder.setMessage("完成购买需要安装银联支付控件，是否安装？");
                builder.setNegativeButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                String filePath = copyApkFromAssets("UPPayPluginEx.apk");
                                if (filePath == null || filePath.isEmpty()) {
                                    showToast(R.string.install_un_error);
                                    return;
                                }
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(new File(filePath)),
                                        "application/vnd.android.package-archive");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });

                builder.setPositiveButton("取消",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            showToast(R.string.un_payment_failure);
        }
    }

    private String copyApkFromAssets(String fileName) {
        try {
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileName;
            File file = new File(filePath);
            if (file.exists())
                return filePath;
            InputStream inputStream = getAssets().open(fileName);
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
            }
            buffer = null;
            inputStream.close();
            outputStream.close();
            return filePath;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void paymentWithWX() {
        Intent intent = new Intent(this, WXPaymentActivity.class);
        startActivityForResult(intent, WXPaymentActivity.REQUEST_CODE);
    }

    /**
     * check some info.the partner,seller etc. 检测配置信息 partnerid商户id，seller收款帐号不能为空
     *
     * @return
     */

    String getOutTradeNo() {

        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss");
        Date date = new Date();
        String strKey = format.format(date);

        java.util.Random r = new java.util.Random();
        strKey = strKey + r.nextInt();
        strKey = strKey.substring(0, 15);
        return strKey;
    }

    /**
     * get the sign type we use. 获取签名方式
     *
     * @return
     */
    String getSignType() {
        String getSignType = "sign_type=" + "\"" + "RSA" + "\"";
        return getSignType;
    }

    // ******支付宝相关处理*****//

    /**
     * get the char set we use. 获取字符集
     *
     * @return
     */
    String getCharset() {
        String charset = "charset=" + "\"" + "utf-8" + "\"";
        return charset;
    }

    protected void closeProgress() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    protected boolean isExsits(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : pinfo) {
            if (packageInfo.packageName.contains(packageName))
                return true;
        }
        return false;
    }

    protected void installUnionApk() {
        AlertDialog.Builder builder = new Builder(this);
        builder.setMessage("您还没有安装全民捷付，是否下载安装？");
        builder.setTitle("提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mpDialog = new ProgressDialog(CheckoutOrderActivity.this);
                mpDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mpDialog.setTitle("提示");
                mpDialog.setMessage("正在下载中，请稍后");
                mpDialog.setIndeterminate(false);
                mpDialog.setCancelable(false);
                mpDialog.setCanceledOnTouchOutside(false);
                mpDialog.setProgress(0);
                mpDialog.incrementProgressBy(1);
                mpDialog.show();
                new Thread() {
                    public void run() {
                        String apkUrl = "http://116.228.21.162:9139/umspaysh/upload/QMJF.apk";
                        URL url = null;
                        try {
                            url = new URL(apkUrl);
                            HttpURLConnection con = (HttpURLConnection) url.openConnection();
                            InputStream in = con.getInputStream();
                            fileSize = con.getContentLength();
                            downloadPath = Environment.getExternalStorageDirectory().getPath() + "/download";
                            File file1 = new File(downloadPath);
                            if (!file1.exists()) {
                                file1.mkdir();
                            }
                            File fileOut = new File(downloadPath + "/umspay.apk");
                            FileOutputStream out = new FileOutputStream(fileOut);
                            byte[] bytes = new byte[1024];
                            mHandler.sendEmptyMessage(START_DOWNLOAD);
                            int c;
                            while ((c = in.read(bytes)) != -1) {
                                out.write(bytes, 0, c);
                                downLoadFileSize += c;
                                mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                            }
                            in.close();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                    }
                }.start();
                dialog.dismiss();

                // if (isExsits(CheckoutOrderActivity.this, "com.hxcr.umspay.activity")) {
                // paymentWithUnionApp();
                // } else {
                // paymentWithUnionSdk();
                // }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (isExsits(CheckoutOrderActivity.this, "com.hxcr.umspay.activity")) {
                    paymentWithUnionApp();
                } else {
                    paymentWithUnionSdk();
                }
            }
        });

        builder.create().show();
    }

    protected void paymentWithUnionApp() {
        Intent intent = new Intent();
        ComponentName cn = new ComponentName("com.hxcr.umspay.activity", "com.hxcr.umspay.activity.Initialize");
        intent.setComponent(cn);
        intent.putExtra("xml", mData.umspay_str);
        startActivityForResult(intent, 0000);
    }

    protected void paymentWithUnionSdk() {
        unionPaymented = true;
        Utils.setPackageName(this.getPackageName());
        Intent intent = new Intent(this, Initialize.class);
        intent.putExtra("xml", mData.umspay_str);
        // 0 for publish 1 for test
        intent.putExtra("istest", "0");
        // startActivityForResult(intent, 0000);
        startActivity(intent);
    }

    protected void paymentWithUnion() {
        if (isExsits(this, "com.hxcr.umspay.activity")) {
            paymentWithUnionApp();
            return;
        }
        // installUnionApk();
        paymentWithUnionSdk();
    }

    protected void paymentWithAlipay() {
        // 检测安全支付服务是否安装
        MobileSecurePayHelper mspHelper = new MobileSecurePayHelper(this);
        boolean isMobile_spExist = mspHelper.detectMobile_sp();
        if (!isMobile_spExist)
            return;

        try {
            MobileSecurePayer msp = new MobileSecurePayer();
            boolean bRet = msp.pay(mData.alipay_str, mHandler, AlixId.RQF_PAY, this);

            if (bRet) {
                // 显示“正在支付”进度条
                closeProgress();
                mDialog = BaseHelper.showProgress(this, null, "正在支付", false, true);
            } else
                ;
        } catch (Exception ex) {
            Toast.makeText(CheckoutOrderActivity.this, R.string.remote_call_failed, Toast.LENGTH_SHORT).show();
        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        if (Utils.getPayResult() != null && unionPaymented) {
            unionPaymented = false;
            String xmlString = Utils.getPayResult();
            if (xmlString.contains(UNIONPAY_SUCCESS)) {
                Toast.makeText(this, getString(R.string.checkout_success), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else if (xmlString.contains(UNIONPAY_FAILURE)) {
                Toast.makeText(this, getString(R.string.checkout_failure), Toast.LENGTH_SHORT).show();
            } else if (xmlString.contains(UNIONPAY_RETRY)) {
                Toast.makeText(this, "支付失败，请重新支付", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WXPaymentActivity.REQUEST_CODE) {
            if (resultCode != WXPaymentActivity.UNSUPPORTED_WX_PAYMENT) {
                return;
            }
            //We popup dialog at here and let user download WeiChat via browser
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            String url = WXInterface.WX_DOWNLOAD_URL;
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setTitle(R.string.tips)
                    .setMessage(R.string.wx_download_tips)
                    .create();
            dialog.show();
            return;
        }
        if (data == null)
            return;
        Bundle bundle = data.getExtras();
        if (bundle == null)
            return;
        String pay_result = bundle.getString("pay_result");
        if (pay_result != null) {
            String msg = "";
            if (pay_result.equalsIgnoreCase("success")) {
                msg = "支付成功！";
            } else if (pay_result.equalsIgnoreCase("fail")) {
                msg = "支付失败！";
            } else if (pay_result.equalsIgnoreCase("cancel")) {
                msg = "用户取消了支付";
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("支付结果通知");
            builder.setMessage(msg);
            builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return;
        }
        String xmlString = bundle.getString("result");
        if (xmlString == null)
            return;
        if (xmlString.contains(UNIONPAY_SUCCESS)) {
            Toast.makeText(this, getString(R.string.checkout_success), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else if (xmlString.contains(UNIONPAY_FAILURE)) {
            Toast.makeText(this, getString(R.string.checkout_failure), Toast.LENGTH_SHORT).show();
        } else if (xmlString.contains(UNIONPAY_RETRY)) {
            Toast.makeText(this, "支付失败，请重新支付", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case AlixId.RQF_PAY: {
                String strRet = (String) msg.obj;
                closeProgress();
                // 处理交易结果
                try {
                    // 获取交易状态码，具体状态代码请参看文档
                    String tradeStatus = "resultStatus={";
                    int imemoStart = strRet.indexOf("resultStatus=");
                    imemoStart += tradeStatus.length();
                    int imemoEnd = strRet.indexOf("};memo=");
                    tradeStatus = strRet.substring(imemoStart, imemoEnd);

                    // //先验签通知
                    // ResultChecker resultChecker = new
                    // ResultChecker(strRet);
                    // int retVal = resultChecker.checkSign();
                    // // 验签失败
                    // if (retVal == ResultChecker.RESULT_CHECK_SIGN_FAILED)
                    // {
                    // BaseHelper.showDialog(
                    // CheckoutOrderActivity.this,
                    // "提示",
                    // getResources().getString(
                    // R.string.check_sign_failed),
                    // android.R.drawable.ic_dialog_alert);
                    // } else {// 验签成功。验签成功后再判断交易状态码
                    // }
                    if (tradeStatus.equals("9000")) { // 判断交易状态码，只有9000表示交易成功
                        BaseHelper.showDialog(CheckoutOrderActivity.this, "提示", "支付成功", android.R.drawable.ic_dialog_info);
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        BaseHelper.showDialog(CheckoutOrderActivity.this, "提示", "支付失败", android.R.drawable.ic_dialog_info);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    BaseHelper.showDialog(CheckoutOrderActivity.this, "提示", strRet, android.R.drawable.ic_dialog_info);
                }
                break;
            }
            case MSG_PAY_RESULT: {
                String strRet = (String) msg.obj; // 支付返回值
                String statusCode = null;
                JSONObject jo;
                try {
                    jo = new JSONObject(strRet);
                    if (jo != null) {
                        statusCode = jo.getString("statusCode");
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

                if (statusCode.equalsIgnoreCase(CheckoutOrderActivity.SUCCESS_CODE)) {
                    // 支付成功
                    Toast.makeText(this, getString(R.string.checkout_success), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else if (statusCode.equalsIgnoreCase(CheckoutOrderActivity.CANCEL_CODE)) {
                    // 用户取消支付
                    Toast.makeText(this, getString(R.string.order_check_opt_cancel), Toast.LENGTH_SHORT).show();
                } else {
                    // 支付失败
                    Toast.makeText(this, getString(R.string.checkout_failure), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case START_DOWNLOAD:
                mpDialog.setMax(100);
                break;
            case UPDATE_PROGRESS:
                int result = (int) (downLoadFileSize * 100 / fileSize);
                mpDialog.setProgress(result);
                break;
            case DOWNLOAD_FINISH:
                mpDialog.setMessage("文件下载完成");
                installApk(downloadPath + "/umspay.apk");
                break;
            case DOWNLOAD_ERROR:
                String error = msg.getData().getString("error");
                mpDialog.setMessage(error);
                break;
            default:
                break;
        }
        return true;
    }

    private class GetTokenIdTask extends AsyncTask<String, Void, String> {

        private String token_id;
        private String err_info;

        public GetTokenIdTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            token_id = null;
            err_info = null;
        }

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            url = url.replace("https:/", "http:/");
            // 获取商户id
            mBargainorId = getBargainorId(url);
            HttpGet request = new HttpGet(url);
            try {
                HttpResponse httpResponse = new DefaultHttpClient().execute(request);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    try {
                        HashMap<String, String> map = XmlUtil.parse(httpResponse.getEntity().getContent());
                        token_id = map.get("token_id");
                        err_info = map.get("err_info");
                        return token_id;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected String getBargainorId(String url) {
            int indexStart, indexEnd;
            if ((indexStart = url.indexOf(BARGAINOR_ID_KEY)) == -1) {
                return new String();
            }
            indexStart += BARGAINOR_ID_KEY.length();
            indexEnd = url.indexOf("&", indexStart);

            return url.substring(indexStart, indexEnd);
        }

        protected void onPostExecute(String id) {
            if (token_id != null) {
                paymentWithTencent(id);
                return;
            }
            if (err_info != null) {
                Toast.makeText(getApplicationContext(), err_info, Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(getApplicationContext(), getString(R.string.checkout_failure), Toast.LENGTH_SHORT).show();
        }

    }

    private class PaymentResultTask extends AsyncTask<String, Void, PaymentResult> {

        public PaymentResultTask() {
            super();
        }

        @Override
        protected PaymentResult doInBackground(String... params) {
            final String key = params[0];
            PaymentResult res = null;
            try {
                res = FactoryCenter.getProcessCenter().getPaymentResult(key);
            } catch (HttpResponseException e) {
                LogUtil.warn(e.getMessage(), e);
            } catch (IOException e) {
                LogUtil.warn(e.getMessage(), e);
            }

            return res;
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(CheckoutOrderActivity.this);
            mDialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    mTask.cancel(true);
                }
            });
            mDialog.setIndeterminate(true);
            mDialog.setMessage(getString(R.string.checkout_progress));
            mDialog.setCancelable(true);
            mDialog.show();
        }

        @Override
        protected void onPostExecute(PaymentResult result) {
            mDialog.dismiss();
            if (result != null && result.status.equalsIgnoreCase("1")) {
                // request success
                Toast.makeText(getApplicationContext(), getString(R.string.checkout_success), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.checkout_failure), Toast.LENGTH_SHORT).show();
            }
        }

    }
}
