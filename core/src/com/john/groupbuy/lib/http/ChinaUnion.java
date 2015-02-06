package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * class for china union
 * Created by qili on 2014/7/11.
 */
public class ChinaUnion implements Parcelable {
    private String tn;
    private String mer_id;
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public String getTn() {
        return tn;
    }

    public void setTn(String tn) {
        this.tn = tn;
    }

    public String getMer_id() {
        return mer_id;
    }

    public void setMer_id(String mer_id) {
        this.mer_id = mer_id;
    }
}
