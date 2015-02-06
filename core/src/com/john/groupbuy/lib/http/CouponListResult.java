package com.john.groupbuy.lib.http;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class CouponListResult implements Parcelable {
    public int status;
    public List<CouponItemInfo> result;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {

    }

}
