package com.john.groupbuy.lib.http;

import com.google.gson.annotations.SerializedName;

public class MyOrderInfoNew {
	public String status;
	@SerializedName("result")
	private MyOrderResult orderData;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public MyOrderResult getOrderData() {
		return orderData;
	}

	public void setOrderData(MyOrderResult orderData) {
		this.orderData = orderData;
	}
}
