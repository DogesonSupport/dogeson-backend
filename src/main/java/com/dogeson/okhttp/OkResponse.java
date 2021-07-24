package com.dogeson.okhttp;

import java.util.HashMap;
import java.util.Map;

public class OkResponse {

    private String url;
    private int statusCode;         //响应状态码
    private Map<String, String> headers = new HashMap<>();
    private String responseBody;

    public String getUrl() {
        return url;
    }

    public OkResponse setUrl(String url) {
        this.url = url;
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public OkResponse setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public OkResponse setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public OkResponse addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public OkResponse setResponseBody(String responseBody) {
        this.responseBody = responseBody;
        return this;
    }
}
