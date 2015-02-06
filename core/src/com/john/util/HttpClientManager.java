
package com.john.util;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

public class HttpClientManager {
    private static HttpParams params;

    private static ClientConnectionManager connectionManager;

    /**
     * 最大连接数
     */
    public final static int MAX_TOTAL_CONNECTIONS = 400;

    /**
     * 获取连接的最大等待时间
     */
    public final static int WAIT_TIMEOUT = 20 * 1000;

    /**
     * 每个路由最大连接数
     */
    public final static int MAX_ROUTE_CONNECTIONS = 400;

    /**
     * 连接超时时间
     */
    public final static int CONNECT_TIMEOUT = 40 * 1000;

    /**
     * 读取超时时间
     */
    public final static int READ_TIMEOUT = 1 * 60 * 1000;

    static {
        params = new BasicHttpParams();
        // 设置客户端版本
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        // 设置字符集
        HttpProtocolParams.setHttpElementCharset(params, HTTP.UTF_8);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        // 默认不激活Expect:100-Continue
        HttpProtocolParams.setUseExpectContinue(params, true);

        HttpConnectionParams.setTcpNoDelay(params, true);
        // 设置 user-agent
        HttpProtocolParams.setUserAgent(params,
                "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) "
                        + "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
        // 设置cookie版本
        params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2965);
        // 设置最大连接数
        ConnManagerParams.setMaxTotalConnections(params, MAX_TOTAL_CONNECTIONS);
        // 设置每个路由最大连接数
        ConnPerRouteBean connPerRoute = new ConnPerRouteBean(MAX_ROUTE_CONNECTIONS);
        ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
        // 超时设置
        /* 从连接池中取连接的超时时间 */
        ConnManagerParams.setTimeout(params, WAIT_TIMEOUT);
        /* 连接超时 */
        HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT);
        /* 请求超时 */
        HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);
        /* Socket 缓存大小 */
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        // 设置我们的HttpClient支持HTTP和HTTPS两种模式
        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        // 使用线程安全的连接管理来创建HttpClient
        connectionManager = new ThreadSafeClientConnManager(params, schReg);
    }

    public static HttpClient getHttpClient() {
        return new DefaultHttpClient(connectionManager, params);
    }
}
