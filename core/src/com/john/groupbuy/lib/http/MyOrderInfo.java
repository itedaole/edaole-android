package com.john.groupbuy.lib.http;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class MyOrderInfo {
	public String status;
	@SerializedName("result")
	private List<MyOrderInfoItem> orderInfoList;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<MyOrderInfoItem> getOrderInfoList() {
		return orderInfoList;
	}

	public void setOrderInfoList(List<MyOrderInfoItem> orderInfoList) {
		this.orderInfoList = orderInfoList;
	}
}
