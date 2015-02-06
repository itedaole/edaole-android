package com.john.groupbuy.lib.http;

import java.util.Locale;

import android.text.TextUtils;

public class CityItem {
	private String name;
	private String id;
	private String pingying;

	public String getCategory() {
		if (TextUtils.isEmpty(pingying))
			return "";
		return pingying.substring(0, 1);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPingying() {
		return pingying;
	}

	public void setPingying(String pingying) {
		this.pingying = pingying.toUpperCase(Locale.US);
	}

}
