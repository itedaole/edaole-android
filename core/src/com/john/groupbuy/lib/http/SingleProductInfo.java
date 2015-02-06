package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.style.ParagraphStyle;


public class SingleProductInfo implements Parcelable, ParagraphStyle {

	public String status;
	public ProductInfo result;
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}

}
