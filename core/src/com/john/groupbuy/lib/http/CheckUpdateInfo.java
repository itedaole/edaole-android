package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;

import com.john.groupbuy.CacheManager;

public class CheckUpdateInfo implements Parcelable {

    private String status; //1-0k,0-fail
    private DataEntity data;
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public boolean isStatusOk(){
        return status.equalsIgnoreCase("1");
    }
    
    public boolean shouldUpdate(){
        if(data != null && data.description != null){
            int version = 0;
            try{
                version = Integer.valueOf(data.description.ver);
                if(version > CacheManager.getInstance().getVersionCode())
                    return true;
            }catch(NumberFormatException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    
    public String getDes(){
        if(data != null && data.description != null)
            return data.description.description;
        return null;
    }
    
    public String getVersionCode(){
        if(data != null && data.description != null)
            return data.description.ver;
        return null;
    }
    
    
    public String getDownloadUrl(){
        if(data != null && data.description != null)
            return data.description.url;
        return null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public final static Parcelable.Creator<CheckUpdateInfo> CREATOR = 
            new Parcelable.Creator<CheckUpdateInfo>() {

        @Override
        public CheckUpdateInfo createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public CheckUpdateInfo[] newArray(int size) {
            return null;
        }
    };
    
    private final class DataEntity{
//        public boolean update;
        public DesEntity description;
    }
    
    private final class DesEntity{
        public String ver;
        public String description;
        public String url;
    }

}
