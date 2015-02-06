package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

public class MyExpressInfo implements Parcelable{
	
	public MyTeam team;
	public String express_no; //快递单号
	public MyExpressName express_name;
	
	public class MyTeam implements Parcelable{
		public String image;

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {

		}
	}
	
	public class MyExpressName implements Parcelable{

		public String id;
		public String name;
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			
		}
		
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}

	public String getExpressName(){
		if(express_name != null){
			return express_name.name;
		}
		return "";
	}
	
	public String getExpressNo(){
		if(express_no != null)
			return express_no;
		return "";
	}

	public String getImageUrl(){
		if(team != null && team.image != null){
			return team.image;
		}
		return "";
	}
}