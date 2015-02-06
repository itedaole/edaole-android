package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

public class VerifyPhoneInfo implements Parcelable {

	public String status;
	public String error;
	public String result;
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}

}
