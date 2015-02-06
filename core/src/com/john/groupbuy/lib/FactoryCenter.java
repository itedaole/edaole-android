package com.john.groupbuy.lib;

public class FactoryCenter {
    private static UserInfoCenter mUserInfoCenter;
    private static ProcessCenter mProduceCenter;

    public static synchronized UserInfoCenter getUserInfoCenter() {
        return mUserInfoCenter == null ? mUserInfoCenter = new UserInfoCenter() : mUserInfoCenter;
    }

    public static synchronized ProcessCenter getProcessCenter() {
        return mProduceCenter == null ? mProduceCenter = new ProcessCenter() : mProduceCenter;
    }

}
