package com.john.groupbuy.lib.http;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class ExpressListInfo implements Parcelable {

	public String status;
	public ResultData result;
	
	public List<MyExpressInfo> getExpressList(){
		if(result!=null){
			return result.express;
		}
		return null;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

	}

}
