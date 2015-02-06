package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * custom data of XG
 * Created by qili on 2014/7/17.
 */
public class XGCustomData implements Parcelable {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
