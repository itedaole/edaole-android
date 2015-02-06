package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

public class ConsumeCouponDataInfo implements Parcelable {

	public String html;
	public String id;
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}

}
