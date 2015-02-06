package com.john.groupbuy.lib.http;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ResultInfo {
	@SerializedName("exitf")
	private PageEntity pageEntity;
	@SerializedName("data")
	private List<ProductInfo> productList;

	public PageEntity getPageEntity() {
		return pageEntity;
	}

	public void setPageEntity(PageEntity pageEntity) {
		this.pageEntity = pageEntity;
	}

	public List<ProductInfo> getProductList() {
		return productList;
	}

	public void setProductList(List<ProductInfo> productList) {
		this.productList = productList;
	}

    public boolean isDataAvailable(){
        return pageEntity != null && productList != null;
    }

    public boolean isDataEmpty(){
        return productList == null || productList.isEmpty();
    }

}
