package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class LoginResult implements Parcelable {

	@SerializedName("status")
	public String mStatus;

	@SerializedName("result")
	public UserInfo mResultInfo;

	@SerializedName("error")
	public ErrorInfo mErrorInfo;


	public static Parcelable.Creator<LoginResult> CREATOR =
			new Creator<LoginResult>() {

		@Override
		public LoginResult createFromParcel(Parcel source) {
			return new LoginResult(source);
		}

		@Override
		public LoginResult[] newArray(int size) {
			return new LoginResult[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mStatus);
		dest.writeParcelable(mResultInfo, flags);
		dest.writeParcelable(mErrorInfo, flags);
	}

	public LoginResult() {
	}

	public LoginResult(Parcel source) {
		mStatus = source.readString();
		mResultInfo = source.readParcelable(UserInfo.class.getClassLoader());
		mErrorInfo = source.readParcelable(ErrorInfo.class.getClassLoader());
	}

}
