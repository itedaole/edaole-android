package com.john.util;

public interface WXInterface {
	final static String ACCESS_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	final static String PAYMENT_URL = "https://api.weixin.qq.com/pay/genprepay?access_token=%s";
	final static String WX_DOWNLOAD_URL = "http://weixin.qq.com/m";
}
