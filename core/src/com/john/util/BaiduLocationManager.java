package com.john.util;

import android.content.Context;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class BaiduLocationManager {
	private static BaiduLocationManager instance = null;
	private  LocationClient client;
	private Context context; //context of application
	
	private BaiduLocationManager(){
		
	}
	
	public static BaiduLocationManager getInstance(){
		if(instance == null){
			instance = new BaiduLocationManager();
		}
		return instance;
	}
	
	public void init(Context appContext){
		setContext(appContext);
		client = new LocationClient(appContext);
		client.setLocOption(getOption());
	}
	
	public void registerLocationListener(BDLocationListener listener){
		if(listener == null)
			return;
		client.registerLocationListener(listener);
	}
	
	public void unRegisterLocationListener(BDLocationListener listener){
		client.unRegisterLocationListener(listener);
	}
	
	public void startService(){
		client.start();
	}
	
	public void stopService(){
		client.stop();
	}
	
	public void requestLocation(){
		client.requestLocation();
	}
	
	protected LocationClientOption getOption() {
		LocationClientOption option = new LocationClientOption();
		// 开启缓存
		option.disableCache(false);
		// 设置GPS开启
		option.setOpenGps(true);
		// 设置返回地址信息
		option.setAddrType("all");
		// 设置为百度坐标系

        option.setCoorType("bd09ll"); // 百度坐标系
		option.setScanSpan(500); // <1000ms 只定位一次，再次定位需要手动请求
		return option;
	}

	public Context getContext() {
	    return context;
    }

	public void setContext(Context context) {
	    this.context = context;
    }
	
}
