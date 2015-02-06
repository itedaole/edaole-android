package com.john.groupbuy.lib;

import java.io.IOException;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.john.groupbuy.CacheManager;
import com.john.groupbuy.lib.http.*;
import com.john.groupbuy.lib.http.ProductListInfo;
import com.john.util.HttpResponseException;
import com.john.util.HttpUtil;

public class ProcessCenter {
    
    public ProductListInfo getProductListByUrl(String url) throws HttpResponseException, IOException {
        ProductListInfo res = HttpUtil.get(url, ProductListInfo.class);
        return res;
    }
    
    public ProductListInfo getNearbyProductList(double lng,double lat,int pageNumber) throws HttpResponseException,
    IOException {
        String url = String.format(Interface.S_PRODUCT_BY_RANGE, lng,lat,pageNumber);
        ProductListInfo res = HttpUtil.get(url, ProductListInfo.class);
        return res;
    }

    public ProductListInfo getCategoryList(String type, int pageNumber, String city_id) throws HttpResponseException,
    IOException {
        String url = null;
        if (city_id == GlobalKey.ALL_CITY_ID) {
            url = String.format(Interface.S_PRODUCT_BY_TYPE, type, pageNumber);
        } else {
            url = String.format(Interface.S_PRODUCT_BY_TYPE, type, pageNumber) + "&city_id=" + city_id;
        }
        ProductListInfo res = HttpUtil.get(url, ProductListInfo.class);
        return res;
    }

    public CityListInfo getCityListInfo() throws HttpResponseException, IOException {
        String url = Interface.S_CITY_List;
        CityListInfo res = HttpUtil.get(url, CityListInfo.class);
        return res;
    }
    
    public HotKeyInfo getHotKeys() throws HttpResponseException, IOException {
    	String url = Interface.DEFAULT_APP_HOST+"Tuan/hotKeys";
    	HotKeyInfo res = HttpUtil.get(url, HotKeyInfo.class);
    	return res;
    }

    public SingleProductInfo refreshProductInfo(String id) throws HttpResponseException, IOException {
        String url = Interface.S_REFRESH_PRODUCT+id;
        SingleProductInfo res = HttpUtil.get(url, SingleProductInfo.class);
        return res;
    }

    public SubmitOrderInfo getSubmitOrderInfo(String arg) throws HttpResponseException, IOException {
        SubmitOrderInfo res = HttpUtil.get(arg, SubmitOrderInfo.class);
        return res;
    }

    public PaymentResult getPaymentResult(String url) throws HttpResponseException, IOException {
        PaymentResult res = HttpUtil.get(url, PaymentResult.class);
        return res;
    }

    public StatusInfo verifyCode(String code) throws HttpResponseException, IOException {
        String url = Interface.S_VERIFY_CODE + code;
        StatusInfo res = HttpUtil.partnerGet(url, StatusInfo.class);
        return res;
    }

    public ConsumeCouponInfo consumeCoupon(String code, String secret) throws HttpResponseException, IOException {
        if (CacheManager.getInstance().isZuitu()){
            String format = Interface.DNS_NAME + "/ajax/coupon.php?action=consume&id=%s&secret=%s";
            String url = String.format(format, code, secret);
            ConsumeCouponInfo res = HttpUtil.partnerGet(url, ConsumeCouponInfo.class);
            return res;
        }else {
            String format = Interface.DEFAULT_APP_HOST + "/Index/coupon&action=consume&id=%s&secret=%s";
            String url = String.format(format, code, secret);
            ConsumeCouponInfo res = HttpUtil.partnerGet(url, ConsumeCouponInfo.class);
            return res;
        }
    }

    public CategoryListInfo getCouponCategory() throws HttpResponseException, IOException {
        String url = Interface.S_COUPON_CATEGORY;
        CategoryListInfo res = HttpUtil.get(url, CategoryListInfo.class);
        return res;
    }
    public ExpressListInfo getExpressListInfo() throws HttpResponseException, IOException {
        String url = Interface.S_EXPRESS_LIST_URL;
        ExpressListInfo res = HttpUtil.get(url, ExpressListInfo.class);
        return res;
    }

    public VerifyPhoneInfo getMobileVerifyInfo(String mobile,String code) throws HttpResponseException, IOException {
        String url = Interface.S_MOBILE_VERIFY;
        url += "mobile=" + mobile;
        if(code.length() != 0){
            url += "&vcode="+code;
        }
        VerifyPhoneInfo res = HttpUtil.get(url, VerifyPhoneInfo.class);
        return res;
    }

    public boolean isLogin() throws HttpResponseException, IOException{
        String url = Interface.S_IS_LOGIN;
        StatusInfo res = HttpUtil.get(url, StatusInfo.class);
        return (res.getStatus().equalsIgnoreCase("1"));
    }
    
    public Object getOrderList(int currentPage,int pageSize) throws HttpResponseException, IOException{
    	String url = String.format(Locale.US,"%s&page=%d&count=%d", Interface.S_GETORDERS,currentPage,pageSize);
    	String jsonString = HttpUtil.get(url, String.class);
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.serializeNulls();
		Gson gson = gsonBuilder.create();
		try {
			return  gson.fromJson(jsonString, MyOrderInfo.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			try {
				return gson.fromJson(jsonString, MyOrderInfoNew.class);
			} catch (JsonSyntaxException e1) {
				e1.printStackTrace();
			}
		}
		return null;
    }
    
    public CheckUpdateInfo getCheckUpdateInfo(String url) throws HttpResponseException, IOException{
        CheckUpdateInfo res = HttpUtil.get(url, CheckUpdateInfo.class);
        return res;
    }

}
