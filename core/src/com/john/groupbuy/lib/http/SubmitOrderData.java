package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

public class SubmitOrderData implements Parcelable {

	//json set fields
	public String price;
	public String fare; //邮费
	public String money;
	public float origin;
	public float credit;
	public String pay_url;
	public String quantity;

	public String token_url;
	public String condbuy;
	public String id;
	public String mobile;
	public String zipcode;
	public String address;
	public String express_id;
	public String alipay_str;
	public String umspay_str;
	public String wx_pay;
	public String upmp_pay;


	//user set fields
//	public String productName;
	@Override
	public int describeContents() {
		return 0;
	}

	public SubmitOrderData(Parcel source){
		price = source.readString();
		fare = source.readString();
		money = source.readString();
		origin = source.readFloat();
		credit = source.readFloat();
		pay_url = source.readString();
		quantity = source.readString();

		token_url = source.readString();
		condbuy = source.readString();
		id = source.readString();
		mobile = source.readString();
		zipcode = source.readString();
		address = source.readString();
		express_id = source.readString();
		alipay_str = source.readString();
		umspay_str = source.readString();
		wx_pay = source.readString();
		upmp_pay = source.readString();

	}

	public SubmitOrderData(MyOrderInfoItem item) {
	       price = item.price;
	        fare = item.fare;
	        money = item.money;
	        origin = item.origin;
	        credit = item.credit;
	        quantity = item.quantity;
	}

	public static Parcelable.Creator<SubmitOrderData> CREATOR =
		new Parcelable.Creator<SubmitOrderData>() {

		@Override
		public SubmitOrderData createFromParcel(Parcel source) {
			return new SubmitOrderData(source);
		}

		@Override
		public SubmitOrderData[] newArray(int size) {
			return new SubmitOrderData[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(price);
		dest.writeString(fare);
		dest.writeString(money);
		dest.writeFloat(origin);
		dest.writeFloat(credit);
		dest.writeString(pay_url);
		dest.writeString(quantity);

		dest.writeString(token_url);
		dest.writeString(condbuy);
		dest.writeString(id);
		dest.writeString(mobile);
		dest.writeString(zipcode);
		dest.writeString(address);
		dest.writeString(express_id);
		dest.writeString(alipay_str);
		dest.writeString(umspay_str);
		dest.writeString(wx_pay);
		dest.writeString(upmp_pay);
	}

}
