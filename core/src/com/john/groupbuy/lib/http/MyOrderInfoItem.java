package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

public class MyOrderInfoItem implements Parcelable {
	
	public String quantity;
	public String price;
	public String money;
	public float origin;
	public float credit;
	public String card;
	public String fare;
	
	public ProductInfo team;

	public static Creator<MyOrderInfoItem> CREATOR = new Creator<MyOrderInfoItem>() {

		@Override
		public MyOrderInfoItem createFromParcel(Parcel source) {
			return null;
		}

		@Override
		public MyOrderInfoItem[] newArray(int size) {
			return null;
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}

}
