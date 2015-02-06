package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

public class CouponItemInfo implements Parcelable {
    public String id;
    public String user_id;
    public String expire_time;
    public String secret;
    public CouponTeam team; //通过id和secret进行验证该优惠券的有效性

    @Override
    public int describeContents() {
        return 0;
    }
    
    public CouponItemInfo(Parcel source){
    	id = source.readString();
    	user_id = source.readString();
    	expire_time = source.readString();
    	secret = source.readString();
    	team = source.readParcelable(CouponTeam.class.getClassLoader());
    }
    
    public static Parcelable.Creator<CouponItemInfo> CREATOR = 
    		new Creator<CouponItemInfo>() {

				@Override
				public CouponItemInfo createFromParcel(Parcel source) {
					return new CouponItemInfo(source);
				}

				@Override
				public CouponItemInfo[] newArray(int size) {
					return new CouponItemInfo[size];
				}
			};

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    	dest.writeString(id);
    	dest.writeString(user_id);
    	dest.writeString(expire_time);
    	dest.writeString(secret);
    	dest.writeParcelable(team, flags);
    }
    

}
