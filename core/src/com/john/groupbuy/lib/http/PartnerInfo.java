package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class PartnerInfo implements Parcelable {
	
	private String id;
	private String username;
	private String title;
	private String phone;
	private String address;
	private String image;
	
	public static Parcelable.Creator<PartnerInfo> CREATOR = new Creator<PartnerInfo>() {

		@Override
		public PartnerInfo createFromParcel(Parcel source) {
			return new PartnerInfo(source);
		}

		@Override
		public PartnerInfo[] newArray(int size) {
			return new PartnerInfo[size];
		}
	};
	
	public PartnerInfo(Parcel source){
		id = (String) source.readValue(String.class.getClassLoader());
		username = (String) source.readValue(String.class.getClassLoader());
		title = (String) source.readValue(String.class.getClassLoader());
		phone = (String) source.readValue(String.class.getClassLoader());
		address = (String) source.readValue(String.class.getClassLoader());
		image = (String) source.readValue(String.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(id);
		dest.writeValue(username);
		dest.writeValue(title);
		dest.writeValue(phone);
		dest.writeValue(address);
		dest.writeValue(image);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getImage() {
		if(!TextUtils.isEmpty(image)){
			if(image.startsWith("http:"))
				return image;
			else{
				return Interface.IMAGE_APP_HOST+image;
			}
		}
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
	
}
