package com.groupbuy.yidaole.wxapi;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.john.groupbuy.CacheManager;
import com.john.groupbuy.lib.http.WXConfig;
import com.john.util.MD5;
import com.john.util.WXInterface;
import com.john.util.WXUtil;
import com.tencent.mm.sdk.modelpay.PayReq;

public class WXPreparePayTask extends BaseTask {

	private final String TAG = "WXPreparePayTask";
	private String accessToken;
	private String nonceStr;
	private String packageValue;
	private long timeStamp;
	private WXConfig wxConfig;

	public WXPreparePayTask(int taskId, Handler handler) {
		super(taskId, handler);
		wxConfig = CacheManager.getInstance().getWxConfig();
	}

	@Override
	protected void onRunningTask() throws JSONException, IOException,
	ClientProtocolException {
		
		if(wxConfig == null)
			throw new IOException("WXConfig is null!");

		if (accessToken == null || accessToken.length() == 0)
			throw new IOException("Access token is NULL");

		String url = String.format(WXInterface.PAYMENT_URL, accessToken);
		String entity = genProductArgs();

		Log.d(TAG, "doInBackground, url = " + url);
		Log.d(TAG, "doInBackground, entity = " + entity);

		byte[] buf = WXUtil.httpPost(url, entity);
		if (buf == null || buf.length == 0) {
			throw new IOException("generate prepare pay info failure");
		}

		String content = new String(buf);
		Log.d(TAG, "doInBackground, content = " + content);
		WXPreparePay preparePay = parser.fromJson(content, WXPreparePay.class);
		if(preparePay == null)
			return;
		String prepareId = preparePay.getPrepayid();
		setObject(generatePayReq(prepareId));
	}

	private PayReq generatePayReq(String prepareId) {

		if(prepareId == null || prepareId.length() == 0 )
			return null;

		PayReq req = new PayReq();
		req.appId = wxConfig.getAppId();
		req.partnerId = wxConfig.getPartnerId();
		req.prepayId = prepareId;
		req.nonceStr = nonceStr;
		req.timeStamp = String.valueOf(timeStamp);
		req.packageValue = "Sign=" + packageValue;

		List<NameValuePair> signParams = new LinkedList<NameValuePair>();
		signParams.add(new BasicNameValuePair("appid", req.appId));
		signParams.add(new BasicNameValuePair("appkey", wxConfig.getSignKey()));
		signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
		signParams.add(new BasicNameValuePair("package", req.packageValue));
		signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
		signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
		signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
		req.sign = genSign(signParams);

		return req;
	}

	private String genNonceStr() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
                .getBytes());
	}

	private long genTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}

	private String getTraceId() {
		return "crestxu_" + genTimeStamp();
	}

	private String genProductArgs() {
		JSONObject json = new JSONObject();

		try {
			json.put("appid", wxConfig.getAppId());
			String traceId = getTraceId();
			json.put("traceid", traceId);
			nonceStr = genNonceStr();
			json.put("noncestr", nonceStr);

//			List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
//			packageParams.add(new BasicNameValuePair("bank_type", "WX"));
//			packageParams.add(new BasicNameValuePair("body", "支付测试"));
//			packageParams.add(new BasicNameValuePair("fee_type", "1"));
//			packageParams.add(new BasicNameValuePair("input_charset", "UTF-8"));
//			packageParams.add(new BasicNameValuePair("notify_url",
//					"http://weixin.qq.com"));
//			packageParams.add(new BasicNameValuePair("out_trade_no",
//					genOutTradNo()));
//			packageParams.add(new BasicNameValuePair("partner", "1900000109"));
//			packageParams.add(new BasicNameValuePair("spbill_create_ip",
//					"196.168.1.1"));
//			packageParams.add(new BasicNameValuePair("total_fee", "1"));
//			packageValue = genPackage(packageParams);

			packageValue = wxConfig.getPakage();
			json.put("package", packageValue);
			timeStamp = genTimeStamp();
			json.put("timestamp", timeStamp);

			List<NameValuePair> signParams = new LinkedList<NameValuePair>();
			signParams.add(new BasicNameValuePair("appid", wxConfig.getAppId()));
			signParams
			.add(new BasicNameValuePair("appkey", wxConfig.getSignKey()));
			signParams.add(new BasicNameValuePair("noncestr", nonceStr));
			signParams.add(new BasicNameValuePair("package", packageValue));
			signParams.add(new BasicNameValuePair("timestamp", String
					.valueOf(timeStamp)));
			signParams.add(new BasicNameValuePair("traceid", traceId));
			json.put("app_signature", genSign(signParams));

			json.put("sign_method", "sha1");
		} catch (Exception e) {
			Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
			return null;
		}

		return json.toString();
	}

	private String genSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		int i = 0;
		for (; i < params.size() - 1; i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append(params.get(i).getName());
		sb.append('=');
		sb.append(params.get(i).getValue());

		String sha1 = WXUtil.sha1(sb.toString());
		Log.d(TAG, "genSign, sha1 = " + sha1);
		return sha1;
	}

//	private String genOutTradNo() {
//		Random random = new Random();
//		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
//				.getBytes());
//	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

//	@SuppressLint("DefaultLocale")
//	private String genPackage(List<NameValuePair> params) {
//		StringBuilder sb = new StringBuilder();
//
//		for (int i = 0; i < params.size(); i++) {
//			sb.append(params.get(i).getName());
//			sb.append('=');
//			sb.append(params.get(i).getValue());
//			sb.append('&');
//		}
//		sb.append("key=");
//		sb.append(wxConfig.getPartnerId());
//
//		String packageSign = MD5.getMessageDigest(sb.toString().getBytes())
//				.toUpperCase();
//		return URLEncodedUtils.format(params, "utf-8") + "&sign=" + packageSign;
//	}

	public WXConfig getConfig() {
		return wxConfig;
	}

}
