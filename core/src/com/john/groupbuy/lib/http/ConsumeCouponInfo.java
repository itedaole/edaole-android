package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

public class ConsumeCouponInfo implements Parcelable {

	public String error;
	public ConsumeCouponData data;
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}
	
	//307558474286 472533
}
