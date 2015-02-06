package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ErrorInfo implements Parcelable {
    @SerializedName("code")
    public String mCode;

    @SerializedName("info")
    public String mInfo;
    
    @SerializedName("text")    
    public String mText;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCode);
        dest.writeString(mInfo);
        dest.writeString(mText);

    }

    public ErrorInfo(Parcel source) {
        mCode = source.readString();
        mInfo = source.readString();
        mText = source.readString();
    }

    public ErrorInfo() {

    }

    public static final Parcelable.Creator<ErrorInfo> CREATOR = new Creator<ErrorInfo>() {

        @Override
        public ErrorInfo createFromParcel(Parcel arg0) {
            return new ErrorInfo(arg0);
        }

        @Override
        public ErrorInfo[] newArray(int size) {
            return new ErrorInfo[size];
        }

    };

}
