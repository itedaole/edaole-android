package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class CouponTeam implements Parcelable {

	public String title;
	public String summary;
	public String image;
	public String product;
	public PartnerInfo partner;
	public String _image_large;
	public String _image_small;

	// create static creator
	public static Parcelable.Creator<CouponTeam> CREATOR = new Creator<CouponTeam>() {
		@Override
		public CouponTeam createFromParcel(Parcel source) {
			return new CouponTeam(source);
		}

		@Override
		public CouponTeam[] newArray(int size) {
			return new CouponTeam[size];
		}
	};

	public CouponTeam(Parcel source) {
		title = (String) source.readValue(String.class.getClassLoader());
		summary =(String) source.readValue(String.class.getClassLoader());
		image = (String) source.readValue(String.class.getClassLoader());
		product = (String) source.readValue(String.class.getClassLoader());
		partner = (PartnerInfo) source.readValue(PartnerInfo.class.getClassLoader());
		_image_large = (String) source.readValue(String.class.getClassLoader());
		_image_small = (String) source.readValue(String.class.getClassLoader());
	}

	public String getLargeImageUrl() {
		if (!TextUtils.isEmpty(_image_large))
			return _image_large;

		if (!TextUtils.isEmpty(image))
			return Interface.IMAGE_APP_HOST + image;

		return null;
	}

	public String getSmallImageUrl() {
		if (!TextUtils.isEmpty(_image_small))
			return _image_small;

		if (!TextUtils.isEmpty(image))
			return Interface.IMAGE_APP_HOST
					+ image.replace(".jpg", "_index.jpg");

		return null;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(title);
		dest.writeValue(summary);
		dest.writeValue(image);
		dest.writeValue(product);
		dest.writeValue(partner);
		dest.writeValue(_image_large);
		dest.writeValue(_image_small);
	}

}
