package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

public class PaymentResult implements Parcelable {
	//如果为1成功，如果为0则失败
	public String status;
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}

}
