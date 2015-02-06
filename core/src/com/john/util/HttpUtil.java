/**
 *
 */

package com.john.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.john.groupbuy.GroupBuyApplication;
import com.john.groupbuy.lib.http.LoginResult;

/**
 * Http常用工具集合
 *
 * @author <a href="mailto:chen.jackson@gmail.com">Mike Chen</a>
 * @version 1.0
 */
public class HttpUtil {

	// private static final String TAG = "HttpUtil";

	private static final String UNIWAP_PROXY_SERVER = "10.0.0.172"; // cmwap、uniwap和3gwap所用代理地址都10.0.0.172:80

	private static final String CTWAP_PROXY_SERVER = "10.0.0.200"; // ctwap所用代理地址为10.0.0.200：80

	private static final String BAD_REQUEST_INFO = "\"info\":\"Bad Request\"";
	private static final String UNAUTHORIZED_INFO = "\"info\":\"Unauthorized\"";
	private static final String SUCCESS_STATUS = "{\"status\":1,\"";

	private static String requestUrl = null;
	private static String loginUrl = null;

	private static boolean isWifi = false; // 当前是否为wifi连接

	private static String apnName = ""; // 如果非wifi连接，当前所使用接入点名称

	private static String cookieValue = null;
	private static String partnerCookieValue = null;

	private final static String SET_COOKIE_VALUE_NAME = "set-cookie";
	private final static String COOKIE_VALUE_NAME = "cookie";
	private final static String PHPSESSID = "PHPSESSID";

	public static String getCookieValue() {
		return cookieValue;
	}

	public static void setCookie(String cookie) {
		cookieValue = cookie;
	}

	public static void setCookieValue(HttpResponse response,boolean isPartner) {
		Header[] headers = response.getHeaders(SET_COOKIE_VALUE_NAME);
        StringBuilder stringBuilder = new StringBuilder();
		for (Header header : headers) {
            stringBuilder.append(header.getValue());
		}
        String cookie = stringBuilder.toString();
        if (cookie.isEmpty())
            return;
        //we don't update cookie when session id is empty
        if (!cookie.contains(PHPSESSID))
            return;
        Log.e("setCookieValue","the value of cookie is \n"+cookie);
        if(isPartner)
            partnerCookieValue = stringBuilder.toString();
        else
            cookieValue = stringBuilder.toString();
	}

	/**
	 * 设置当前网络是否为WIFI网络
	 *
	 * @param isWifi
	 */
	public static void setWifi(boolean isWifi) {
		HttpUtil.isWifi = isWifi;
	}

	/**
	 * 是否为WIFI
	 *
	 * @return
	 */
	public static boolean isWifi() {
		return isWifi;
	}

	/**
	 * 设置当前网络的接点名称
	 *
	 * @param apnName
	 */
	public static void setApnName(String apnName) {
		HttpUtil.apnName = apnName;
	}

	/**
	 * 从指定的URL地址获取JSON数据，并以MAP的方式返回结果
	 *
	 * @param url
	 *            目标服务器地址
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getJSON(String url)
			throws ParseException, IOException, HttpResponseException {
		return getJSON(url, null, Map.class);
	}

	/**
	 * 从指定的URL地址获取JSON数据
	 *
	 * @param url
	 *            服务器目标地址
	 * @param clazz
	 *            返回的结果类
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public static <T> T getJSON(String url, Class<T> clazz)
			throws ParseException, IOException, HttpResponseException {
		return getJSON(url, null, clazz);
	}

	/**
	 * 从指定的URL地址获取JSON数据
	 *
	 * @param url
	 *            目标地址
	 * @param data
	 *            发送给服务器的数据
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getJSON(String url,
			Map<String, String> data) throws ParseException, IOException,
			HttpResponseException {
		return getJSON(url, data, Map.class);
	}

	/**
	 * 从指定的URL地址获取JSON数据
	 *
	 * @param url
	 *            目标地址
	 * @param data
	 *            发送给服务器的数据
	 * @param clazz
	 *            返回的结果类
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public static <T> T getJSON(String url, Map<String, String> data,
			Class<T> clazz) throws ParseException, IOException,
			HttpResponseException {

            if (data != null && !data.isEmpty()) {
                String paramStr = "";
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    paramStr += "&" + entry.getKey() + "=" + entry.getValue();
                }

			if (url.indexOf("?") == -1) {
				url += paramStr.replaceFirst("&", "?");
			} else {
				url += paramStr;
			}
		}

		HttpGet request = new HttpGet(url);
		LogUtil.debug(url);
		// 添加Gzip支持
		supportGzip(request);

		// 检查是否需要设置代理
		checkProxy(request);

		try {
			return processResponse(
					HttpClientManager.getHttpClient().execute(request), clazz,
					true,false);
		} catch (NullPointerException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * 从指定的URL地址获取JSON数据
	 *
	 * @param <T>
	 * @param url
	 * @param clazz
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public static <T> List<T> getJSONArray(String url, Class<T> clazz)
			throws ParseException, IOException, HttpResponseException {
		return getJSONArray(url, null, clazz);
	}

	/**
	 * 从指定的URL地址获取JSON数据
	 *
	 * @param <T>
	 * @param url
	 * @param data
	 * @param clazz
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public static <T> List<T> getJSONArray(String url,
			Map<String, String> data, Class<T> clazz) throws ParseException,
			IOException, HttpResponseException {
		HttpGet request = null;
		if (data == null || data.isEmpty()) {
			request = new HttpGet(url);
		} else {
			String paramStr = "";
			for (Map.Entry<String, String> entry : data.entrySet()) {
				paramStr += "&" + entry.getKey() + "=" + entry.getValue();
			}

			if (url.indexOf("?") == -1) {
				url += paramStr.replaceFirst("&", "?");
			} else {
				url += paramStr;
			}

			request = new HttpGet(url);
		}

		// 添加Gzip支持
		supportGzip(request);

		// 检查是否需要设置代理
		checkProxy(request);

		HttpResponse response;
		try {
			response = HttpClientManager.getHttpClient().execute(request);
		} catch (NullPointerException e) {
			throw new IOException(e.getMessage());
		}
		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode == HttpStatus.SC_OK) {
			String result;
			try {
				HttpEntity entity = response.getEntity();
				result = null;
				if (isSupportGzip(response)) {
					InputStream is = new GZIPInputStream(entity.getContent());
					Reader reader = new InputStreamReader(is,
							EntityUtils.getContentCharSet(entity));
					CharArrayBuffer buffer = new CharArrayBuffer(
							(int) entity.getContentLength());
					try {
						char[] tmp = new char[1024];
						int l;
						while ((l = reader.read(tmp)) != -1) {
							buffer.append(tmp, 0, l);
						}
					} finally {
						reader.close();
					}
					result = buffer.toString();
				} else {
					result = EntityUtils.toString(entity);
				}

				entity.consumeContent(); // 释放或销毁内容

				// Log.d("HttpUtills", "recv:" + result);
				Type listType = new TypeToken<List<T>>() {
				}.getType();
				GsonBuilder gBuilder = new GsonBuilder();
				gBuilder.registerTypeAdapter(listType, new ListTypeAdapter<T>(
						clazz));
				return gBuilder.create().fromJson(result, listType);
				// return JSON.parseArray(result, clazz);
			} catch (OutOfMemoryError e) {
				// Log.d("HttpUtil", "getJSONArray", e);
				System.gc();
			}
			return null;
		} else {
			throw new HttpResponseException(statusCode);
		}
	}

	/**
	 * 请求数据，并返回JSON对象
	 *
	 * @param <T>
	 * @param url
	 * @param clazz
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public static <T> T get(String url, Class<T> clazz) throws ParseException,
			IOException, HttpResponseException {
        url = url.replace(" ","%20");
		HttpGet request = new HttpGet(url);
		if (cookieValue != null)
			request.setHeader(COOKIE_VALUE_NAME, cookieValue);
		LogUtil.debug("get url:" + url);

		if (url.contains("User/login")) {
			loginUrl = url;
		}

		requestUrl = url;

		try {
			return processResponse(
					HttpClientManager.getHttpClient().execute(request), clazz,
					true,false);
		} catch (UnsupportedEncodingException e) {
			throw new ParseException(e.getMessage());
		} catch (NullPointerException e) {
			throw new IOException(e.getMessage());
		}
	}

	public static <T> T partnerGet(String url, Class<T> clazz) throws ParseException,
	IOException, HttpResponseException {
		HttpGet request = new HttpGet(url);
		if (partnerCookieValue != null)
			request.setHeader(COOKIE_VALUE_NAME, partnerCookieValue);
		try {
			return processResponse(
					HttpClientManager.getHttpClient().execute(request), clazz,
					true,true);
		} catch (UnsupportedEncodingException e) {
			throw new ParseException(e.getMessage());
		} catch (NullPointerException e) {
			throw new IOException(e.getMessage());
		}
	}

	protected static <T> T privateGet(String url, Class<T> clazz)
			throws ParseException, IOException, HttpResponseException {
		HttpGet request = new HttpGet(url);
		if (cookieValue != null)
			request.setHeader(COOKIE_VALUE_NAME, cookieValue);
		LogUtil.debug("get url:" + url);

		try {
			return processResponse(
					HttpClientManager.getHttpClient().execute(request), clazz,
					false,false);
		} catch (UnsupportedEncodingException e) {
			throw new ParseException(e.getMessage());
		} catch (NullPointerException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * 上传文件
	 *
	 * @param url
	 *            目标服务器地址
	 * @param data
	 *            附加的参数映射
	 * @param files
	 *            需要上传的文件列表
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> uploadFile(String url,
			Map<String, String> data, Map<String, File> files)
			throws ParseException, IOException, HttpResponseException {
		return uploadFile(url, data, files, Map.class);
	}

	/**
	 * 上传文件
	 *
	 * @param url
	 *            目标服务器地址
	 * @param data
	 *            附加的参数映射
	 * @param files
	 *            需要上传的文件列表
	 * @param clazz
	 *            返回的结果类
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws HttpResponseException
	 */
	public static <T> T uploadFile(String url, Map<String, String> data,
			Map<String, File> files, Class<T> clazz) throws ParseException,
			IOException, HttpResponseException {
		HttpPost request = new HttpPost(url);

		// 添加Gzip支持
		supportGzip(request);

		// 检查是否需要设置代理
		checkProxy(request);

		Charset charset = Charset.forName(HTTP.UTF_8);

		MultipartEntity entity = new MultipartEntity();

		// 添加传回参数
		if (data != null && !data.isEmpty()) {
			for (Map.Entry<String, String> entry : data.entrySet()) {
				entity.addPart(entry.getKey(), new StringBody(entry.getValue(),
						charset));
			}
		}

		// 添加文件
		if (files != null && !files.isEmpty()) {
			for (Map.Entry<String, File> entry : files.entrySet()) {
				entity.addPart(entry.getKey(), new FileBody(entry.getValue()));
			}
		}
		try {
			request.setEntity(entity);
			HttpResponse response = HttpClientManager.getHttpClient().execute(
					request);
			return processResponse(response, clazz, true,false);
		} catch (NullPointerException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * 添加Gzip压缩支持
	 *
	 * @param request
	 */
	private static void supportGzip(HttpRequest request) {
		// 添加对gzip的支持
		request.addHeader("Accept-Encoding", "gzip");
	}

	/**
	 * 判断服务器是否支持gzip压缩
	 *
	 * @param response
	 * @return
	 */
	public static boolean isSupportGzip(HttpResponse response) {
		Header contentEncoding = response.getFirstHeader("Content-Encoding");

		return contentEncoding != null
				&& contentEncoding.getValue().equalsIgnoreCase("gzip");
	}

	/**
	 * 检查是否需要代理设置
	 *
	 * @return
	 */
	public static String getProxyUrl() {
		if (ApnNet.CMWAP.equals(apnName) || ApnNet.UNIWAP.equals(apnName)
				|| ApnNet.GWAP_3.equals(apnName)) {
			return UNIWAP_PROXY_SERVER;
		} else if (ApnNet.CTWAP.equals(apnName)) {
			return CTWAP_PROXY_SERVER;
		}
		return "";
	}

	/**
	 * 检查是否需要代理设置
	 *
	 * @return
	 */
	public static boolean checkNeedProxy() {
		if (ApnNet.CMWAP.equals(apnName) || ApnNet.UNIWAP.equals(apnName)
				|| ApnNet.GWAP_3.equals(apnName)) {
			return true;
		} else if (ApnNet.CTWAP.equals(apnName)) {
			return true;
		}
		return false;
	}

	/**
	 * 检查是否设置代理，当前非WIFI连接时，如果接入点为cmwap、uniwap、ctwap和3gwap，则需要设置代理主机地址（cmwap、
	 * uniwap和3gwap所用代理地址都10.0.0.172:80，ctwap所用代理地址为10.0.0.200：80 ）
	 *
	 * @param request
	 */
	public static void checkProxy(HttpRequest request) {
		if (isWifi)
			return;

		if (ApnNet.CMWAP.equals(apnName) || ApnNet.UNIWAP.equals(apnName)
				|| ApnNet.GWAP_3.equals(apnName)) {
			HttpHost proxy = new HttpHost(UNIWAP_PROXY_SERVER, 80);
			ConnRouteParams.setDefaultProxy(request.getParams(), proxy);
		} else if (ApnNet.CTWAP.equals(apnName)) {
			HttpHost proxy = new HttpHost(CTWAP_PROXY_SERVER, 80);
			ConnRouteParams.setDefaultProxy(request.getParams(), proxy);
		}
	}

	/**
	 * 处理响应结果
	 */
	@SuppressWarnings("unchecked")
    private static <T> T processResponse(HttpResponse response, Class<T> clazz,
			boolean retryFlag,boolean isPartner) throws ParseException, IOException,
			HttpResponseException {
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == HttpStatus.SC_OK) {
            //set cookie value from http response
            setCookieValue(response,isPartner);
			HttpEntity entity = response.getEntity();
			String result = null;
			if (isSupportGzip(response)) {
				InputStream is = new GZIPInputStream(entity.getContent());
				Reader reader = new InputStreamReader(is,
						EntityUtils.getContentCharSet(entity));
				CharArrayBuffer buffer = new CharArrayBuffer(
						(int) entity.getContentLength());
				try {
					char[] tmp = new char[1024];
					int l;
					while ((l = reader.read(tmp)) != -1) {
						buffer.append(tmp, 0, l);
					}
				} finally {
					reader.close();
				}
				result = buffer.toString();
				LogUtil.debug(result);
			} else {
				result = EntityUtils.toString(entity);
			}

			entity.consumeContent(); // 释放或销毁内容

			if (TextUtils.isEmpty(result)) {
				return null;
			}

			android.util.Log.e("result json is ",result);
			if (retryFlag
					&& GroupBuyApplication.sIsUserLogin
					&& !requestUrl.contains("/login&")
					&& (result.contains(BAD_REQUEST_INFO) || result
							.contains(UNAUTHORIZED_INFO))) {
				// auto login
				String rs = HttpUtil.privateGet(loginUrl, String.class);
				if (rs.contains(SUCCESS_STATUS)) {
					// if auto login success then request Previous url for
					// result
					return HttpUtil.privateGet(requestUrl, clazz);
				}
			}

			// 将返回的文本结果进行json解析
			GsonBuilder gsonb = new GsonBuilder();
			gsonb.serializeNulls();
			Gson gson = gsonb.create();
			try {
				if (clazz.equals(Map.class)) {
					Type mapType = new TypeToken<Map<String, String>>() {
					}.getType();
					return gson.fromJson(result, mapType);
				} else if (clazz.equals(String.class)) {
					return (T) result;
				} else {
					return gson.fromJson(result, clazz);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new IOException();
			}
			// return JSON.parseObject(result, clazz);
		} else {
			throw new HttpResponseException(statusCode);
		}
	}

	/**
	 * 根据url和 数据内容get方式提交
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws Exception
	 */
	public static String postData(String url) throws ClientProtocolException,
			IOException {
		HttpGet request = new HttpGet(url);
		checkProxy(request);
		HttpResponse hr = HttpClientManager.getHttpClient().execute(request);
		HttpEntity entity = hr.getEntity();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				entity.getContent()));
		String buff = null;
		StringBuilder sb = new StringBuilder();
		while ((buff = br.readLine()) != null)
			sb.append(buff);
		return sb.toString();
	}
}
