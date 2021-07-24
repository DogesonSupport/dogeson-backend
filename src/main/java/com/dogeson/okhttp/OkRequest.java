package com.dogeson.okhttp;

import java.util.HashMap;
import java.util.Map;

public class OkRequest {

    private String method;
    private String url;
    private Map<String, String> headers = new HashMap<>();
    private String body;

    private int connectTimeout = 30000;
    private int readTimeout = 45000;

    public String getMethod() {
        return this.method;
    }

    public OkRequest setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getUrl() {
        return this.url;
    }

    public OkRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public OkRequest setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public OkRequest addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public OkRequest addHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public String getBody() {
        return this.body;
    }

    public OkRequest setBody(String body) {
        this.body = body;
        return this;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public OkRequest setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    public OkRequest setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }
}
