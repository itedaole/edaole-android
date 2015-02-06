package com.john.groupbuy.lib.http;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.john.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ProductInfo implements Parcelable {

    public String id; //商品id
    public String user_id;
    public String title; //标题
    public String team_jybt; //建议标题
    public String summary; //总结
    public String city_id;
    public String city_ids;
    public String group_id;
    public String system;
    public String team_price; //优惠价格
    public String market_price; //市场价格
    public String expire_time; //优惠券到期时间
    public String begin_time; //开始时间
    public String end_time; //终止时间
    public String product;//商品标题-用于列表中显示
    public String condbuy; //商品型号
    public String mobile; //电话号码
    public String per_number;
    public String permin_number;
    public String min_number;
    public String max_number;
    public String now_number;//现在优惠的人数
    public String pre_number;
    public String allowrefund;
    public String image; //商品图片
    public String notice;
    public String reach_time;
    public PartnerInfo partner;
    @SerializedName("tksq-gm")
    public String tksq_gm; //退款
    @SerializedName("tksq-gq")
    public String tksq_gq; //过期退款
    public String bonus; //邀请返利
    public ExpressInfo express_relate[];//快递相关信息
    //与定位相关的信息
    public float _lng;
    public float _lat;
    public float _range;

    private String _image_large;
    private String _image1_large;
    private String _image2_large;

    private String _image_small;
    private String _image1_small;
    private String _image2_small;


    public static final Parcelable.Creator<ProductInfo> CREATOR
    = new Parcelable.Creator<ProductInfo>() {

    	@Override
    	public ProductInfo createFromParcel(Parcel source) {
    		return null;
    	}

    	@Override
    	public ProductInfo[] newArray(int size) {
    		return new ProductInfo[size];
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

    public List<String> getPreviewImages()
    {
        List<String> images = new ArrayList<String>();
        images.add(_image_large);
        images.add(_image1_large);
        images.add(_image2_large);
        return images;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public String getMarketPrice(){
    	return Utility.trimFloatStringZero(market_price);
    }
    
    public String getTeamPrice(){
    	return Utility.trimFloatStringZero(team_price);
    }
}
