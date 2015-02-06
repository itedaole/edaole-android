package com.john.groupbuy.lib.http;


import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class CategoryListInfo implements Parcelable {

    public String status;
    public List<CategoryInfo> result;
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

}
