package com.highest.base.http;

import com.highest.base.http.cookie.CookieStorePool;
import com.highest.base.http.httpclient.DefaultHttpClientPool;
import com.highest.base.http.httpclient.HttpClientExecutor;
import com.highest.base.http.httpclient.HttpClientFactory;
import com.highest.base.http.httpclient.HttpClientPool;
import com.highest.base.http.httpclient.response.Response;
import com.highest.base.http.proxy.HttpProxyPool;

public class HttpHelper {

    private static HttpClientPool httpClientPool;

    static {
        HttpHelper.httpClientPool = new DefaultHttpClientPool(HttpClientFactory.create());
    }

    public static <T> Response<T> execute(SiteConfig siteConfig, Request request) {
        HttpClientExecutor executor = new HttpClientExecutor(
                httpClientPool,
                null,
                null,
                siteConfig,
                request);

        return executor.execute();
    }

    public static <T> Response<T> execute(SiteConfig siteConfig, Request request, CookieStorePool cookieStorePool) {
        HttpClientExecutor executor = new HttpClientExecutor(
                httpClientPool,
                null,
                cookieStorePool,
                siteConfig,
                request);

        return executor.execute();
    }

    public static <T> Response<T> execute(SiteConfig siteConfig, Request request, HttpProxyPool httpProxyPool, CookieStorePool cookieStorePool) {
        HttpClientExecutor executor = new HttpClientExecutor(
                httpClientPool,
                httpProxyPool,
                cookieStorePool,
                siteConfig,
                request);

        return executor.execute();
    }
}
