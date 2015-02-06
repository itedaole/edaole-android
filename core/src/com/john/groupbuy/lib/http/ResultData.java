package com.john.groupbuy.lib.http;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class ResultData implements Parcelable{
	public List<MyCoupon> coupons;
	public List<MyExpressInfo> express;
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}
}
