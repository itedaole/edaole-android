package com.groupbuy.yidaole.wxapi;

import java.io.IOException;

import com.john.util.WXInterface;
import com.john.util.WXUtil;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.os.Handler;

import com.john.groupbuy.CacheManager;
import com.john.groupbuy.lib.http.WXConfig;

public class WXTokenTask extends BaseTask {
	private WXConfig wxConfig;

	public WXTokenTask(int taskId, Handler handler) {
		super(taskId, handler);
		wxConfig = CacheManager.getInstance().getWxConfig();
	}

	@Override
	protected void onRunningTask() throws JSONException, IOException,
			ClientProtocolException {
		
		if(wxConfig == null)
			throw new IOException("WXConfig is null!");
		
		String url = String.format(WXInterface.ACCESS_TOKEN,
				wxConfig.getAppId(),wxConfig.getAppSecret());
		byte[] buf = WXUtil.httpGet(url);
		if (buf == null || buf.length == 0)
			throw new IOException("Get access token failure!");
		String jsonString = new String(buf);
		setObject(parser.fromJson(jsonString, WXToken.class));
	}

}
