package com.john.groupbuy.lib.http;


import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class SingleCouponInfo_Result implements Serializable {
	private static final long serialVersionUID = -1861755620178978569L;
	public String id; //商品id
    public String title; //标题
    public String title_local;
    public String shop_name;
    public String address;
    public String yy_time;
    public String phone;
    public String couponcode;
    public String share_summary;
    
    public String image;
    public String large_image;
    private String _image_large;
    private String _image_small;
    
    public String share_url;


    public static final Parcelable.Creator<SingleCouponInfo_Result> CREATOR
    = new Parcelable.Creator<SingleCouponInfo_Result>() {

    	@Override
    	public SingleCouponInfo_Result createFromParcel(Parcel source) {
    		return null;
    	}

    	@Override
    	public SingleCouponInfo_Result[] newArray(int size) {
    		return new SingleCouponInfo_Result[size];
    	}
    };
    
    public String getLargeImageUrl(){
    	if(!TextUtils.isEmpty(_image_large))
    		return _image_large;
    	
    	if(!TextUtils.isEmpty(image))
    		return Interface.IMAGE_APP_HOST + image;
    	
    	return null;
    }
    
    public String getSmallImageUrl(){
    	if(!TextUtils.isEmpty(_image_small))
    		return _image_small;
    	
    	if(!TextUtils.isEmpty(image))
    		return Interface.IMAGE_APP_HOST + image.replace(".jpg", "_index.jpg");
    	
    	return null;
    }
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//
//    }
}
