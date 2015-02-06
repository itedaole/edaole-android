package com.john.groupbuy.lib.http;

import com.google.gson.annotations.SerializedName;


public class StatusInfo  {

	private String status; //1 
	private String result; //success
	@SerializedName("team")
	private ProductInfo productInfo;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public ProductInfo getProductInfo() {
		return productInfo;
	}
	public void setProductInfo(ProductInfo productInfo) {
		this.productInfo = productInfo;
	}
}
