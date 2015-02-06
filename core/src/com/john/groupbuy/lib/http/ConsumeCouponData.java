package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

public class ConsumeCouponData implements Parcelable {
	
	public ConsumeCouponDataInfo data;
	public String type;
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}

}
