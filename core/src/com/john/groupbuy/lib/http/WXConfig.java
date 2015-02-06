package com.john.groupbuy.lib.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class WXConfig {
	private String AppId;
	private String PartnerId;
	private String SignKey;
	private String AppSecret;
	private String pakage;
	
	public WXConfig() {
	}
	
	public static WXConfig fromJson(String json){
		if(json == null || json.length() == 0)
			return null;
		Gson  gsonParser = new GsonBuilder().serializeNulls().create();
		try {
			return  gsonParser.fromJson(json, WXConfig.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	public String getAppId() {
		return AppId;
	}
	public void setAppId(String appId) {
		AppId = appId;
	}
	public String getPartnerId() {
		return PartnerId;
	}
	public void setPartnerId(String partnerId) {
		PartnerId = partnerId;
	}
	public String getSignKey() {
		return SignKey;
	}
	public void setSignKey(String signKey) {
		SignKey = signKey;
	}
	public String getAppSecret() {
		return AppSecret;
	}
	public void setAppSecret(String appSecret) {
		AppSecret = appSecret;
	}
	public String getPakage() {
		return pakage;
	}
	public void setPakage(String pakage) {
		this.pakage = pakage;
	}

}
