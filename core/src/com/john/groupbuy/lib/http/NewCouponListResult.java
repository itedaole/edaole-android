package com.john.groupbuy.lib.http;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class NewCouponListResult implements Parcelable {
    public int status;
    public List<NewCouponListResult_Result> result;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {

    }

}
