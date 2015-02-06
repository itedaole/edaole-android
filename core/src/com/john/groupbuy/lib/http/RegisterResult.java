package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

public class RegisterResult implements Parcelable {
    public int status;
    public ErrorInfo error;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

}
