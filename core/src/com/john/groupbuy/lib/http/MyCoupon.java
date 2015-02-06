package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

public class MyCoupon implements Parcelable{
	public String id;
	public String user_id;
	public String partner_id;
	public String order_id;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}
	
}
