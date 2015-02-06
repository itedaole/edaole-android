package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

public class SubmitOrderInfo implements Parcelable {

//	public final static String STATUS_SUCCESS = "1";
	
	public String status;
	public SubmitOrderData result;
	public ErrorInfo error;
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}

}
