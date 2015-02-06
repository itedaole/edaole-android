package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

public class ExpressInfo implements Parcelable {

	public String id;
	public String price;
	public String name;
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}

}
