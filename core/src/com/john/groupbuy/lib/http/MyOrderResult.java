package com.john.groupbuy.lib.http;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class MyOrderResult {
	@SerializedName("exitf")
	private PageEntity pageEntity;
	@SerializedName("data")
	private List<MyOrderInfoItem> orderList;

	public PageEntity getPageEntity() {
		return pageEntity;
	}

	public void setPageEntity(PageEntity pageEntity) {
		this.pageEntity = pageEntity;
	}

	public List<MyOrderInfoItem> getOrderList() {
		return orderList;
	}

	public void setOrderList(List<MyOrderInfoItem> orderList) {
		this.orderList = orderList;
	}

}
