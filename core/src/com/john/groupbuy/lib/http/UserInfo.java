package com.john.groupbuy.lib.http;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class UserInfo implements Parcelable {
    @SerializedName("id")
    public int id;
    public String email;
    private String username;
    private String realname;
    public String avatar;
    public String alipay_id;
    public String gender;
    public String newbie;
    public String mobile;
    public String qq;
    private String money;
    public String score;
    public String zipcode;
    public String address;
    public String city_id;
    public String emailable;
    public String enable;
    public String mananger;
    public String recode;
    public String sns;
    public String ip;
    public String mobilecode;
    public UserConfig _config;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public static Parcelable.Creator<UserInfo> CREATOR = new Parcelable.Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public UserInfo[] newArray(int size) {
            return null;
        }
    };

    public boolean shouldBindingPhone() {
        if (_config != null && !TextUtils.isEmpty(_config.requireMobile))
            return _config.requireMobile.equalsIgnoreCase("1");
        return true;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public static class UserConfig {
        String requireMobile;
    }
}
