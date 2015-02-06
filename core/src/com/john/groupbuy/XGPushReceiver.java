package com.john.groupbuy;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.john.groupbuy.lib.http.XGCustomData;
import com.tencent.android.tpush.*;

/**
 * The push receiver of XG
 * Created by qili on 2014/7/17.
 */
public class XGPushReceiver extends XGPushBaseReceiver {
    @Override
    public void onRegisterResult(Context context, int i, XGPushRegisterResult xgPushRegisterResult) {
        //do nothing
    }

    @Override
    public void onUnregisterResult(Context context, int i) {
        //do nothing
    }

    @Override
    public void onSetTagResult(Context context, int i, String s) {
        //do nothing
    }

    @Override
    public void onDeleteTagResult(Context context, int i, String s) {
        //do nothing
    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage message) {
        //do nothing
    }

    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult xgPushClickedResult) {
        if (context == null || xgPushClickedResult == null)
            return;
        String customContent = xgPushClickedResult.getCustomContent();
        if (customContent == null || customContent.length() == 0)
            return;
        Gson gson = new GsonBuilder().serializeNulls().create();
        try {
            XGCustomData customData = gson.fromJson(customContent,XGCustomData.class);
            String id = customData.getId();
            if (id == null || id.length() == 0)
                return;
            CacheManager.getInstance().setCustomId(id);
//            Intent intent = new Intent("com.john.groupbuy.ShowProduct");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra(ProductActivity.KEY_PRODUCT_ID,id);
//            context.startActivity(intent);
        }catch (JsonSyntaxException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult xgPushShowedResult) {
        //do nothing
    }
}
