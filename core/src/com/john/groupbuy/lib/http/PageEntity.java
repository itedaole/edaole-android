package com.john.groupbuy.lib.http;

import com.google.gson.annotations.SerializedName;

public class PageEntity {
	private int count;
	@SerializedName("page_current")
	private int currentPage;
	@SerializedName("page_count")
	private int pageCount;
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public int getPageCount() {
		return pageCount;
	}
	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}
	
	public boolean isLastPage(){
		return currentPage == pageCount;
	}

    public boolean isFirstPage(){
        return currentPage == 1 || currentPage == 0;
    }
}
