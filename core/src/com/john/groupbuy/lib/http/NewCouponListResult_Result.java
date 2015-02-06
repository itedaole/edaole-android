package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

public class NewCouponListResult_Result implements Parcelable {
	public int id;
	public int user_id;
	public String title;
	public String image;

	public String image1;
	public String image2;
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
}
