package com.dogeson.okhttp;

import org.apache.http.client.CookieStore;

public class OkHttpHelper {

    public static OkResponse execute(OkRequest request, CookieStore cookieStore) {
        return new HttpClientExecutor(request, cookieStore).execute();
    }

    public static OkResponse execute(OkRequest request) {
        return new HttpClientExecutor(request, null).execute();
    }
}
