package com.john.groupbuy.lib.http;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

//类型列表中的信息
public class CategoryInfo implements Parcelable {

	public final static String CATEGORY_ALL = "0";
	public final static String CATEGORY_NEW = "new";

	public String id;
	public String name;
	public String fid;
	private List<CategoryInfo> subClasss;
	private int mainIndex = -1;
	private int subIndex = -1;

	public static Parcelable.Creator<CategoryInfo> CREATOR = new Creator<CategoryInfo>() {

		public CategoryInfo createFromParcel(Parcel source) {
			return new CategoryInfo(source);
		}

		@Override
		public CategoryInfo[] newArray(int size) {
			return new CategoryInfo[size];
		}

	};
	
	CategoryInfo(CategoryInfo other){
		id = other.id;
		name = other.name;
		fid = other.fid;
	}
	
	public boolean isAvailable(){
		return (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(name));
	}

	public void insertSelfCategory(){
		if(subClasss != null && !subClasss.isEmpty()){
			CategoryInfo self = new CategoryInfo(this);
			List<CategoryInfo> list = new ArrayList<CategoryInfo>();
			list.add(new CategoryInfo(self));
			for (CategoryInfo category : subClasss) {
                category.id = String.format("%s@%s", id,category.id);
            }
			list.addAll(subClasss);
			subClasss = list;
		}else{
			subClasss = new ArrayList<CategoryInfo>();
		}
	}
	public String getDisplayName() {
		if (TextUtils.isEmpty(name))
			return "";

		if (name.length() <= 3) {
			return name;
		} else {
			return name.substring(0, 2) + "\n" + name.substring(2);
		}
	}

	public CategoryInfo() {
		super();
	}

	public CategoryInfo(String name, String id) {
		super();
		this.name = name;
		this.id = id;
	}

	@SuppressWarnings("unchecked")
	public CategoryInfo(Parcel source) {
		id = source.readString();
		name = source.readString();
		fid = source.readString();
		setSubClasss(source.readArrayList(CategoryInfo.class.getClassLoader()));
		mainIndex = source.readInt();
		subIndex = source.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(fid);
		dest.writeList(subClasss);
		dest.writeInt(mainIndex);
		dest.writeInt(subIndex);
	}

	public List<CategoryInfo> getSubClasss() {
		return subClasss;
	}

	public void setSubClasss(List<CategoryInfo> subClasss) {
		this.subClasss = subClasss;
	}

	public int getMainIndex() {
		return mainIndex;
	}

	public void setMainIndex(int mainIndex) {
		this.mainIndex = mainIndex;
	}

	public int getSubIndex() {
		return subIndex;
	}

	public void setSubIndex(int subIndex) {
		this.subIndex = subIndex;
	}
	
	
}
