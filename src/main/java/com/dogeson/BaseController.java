package com.dogeson;

import com.dogeson.okhttp.OkHttpHelper;
import com.dogeson.okhttp.OkRequest;
import com.dogeson.okhttp.OkResponse;

import java.util.List;
import java.util.Map;

public class BaseController {

    public OkResponse fetch(String url) throws Exception {
        OkResponse okResponse;
        okResponse = OkHttpHelper.execute(
                new OkRequest()
                        .setMethod("GET")
                        .setUrl(url)
                        .addHeader("user-agent", "MTOPSDK%2F3.1.1.7+%28Android%3B8.1.0%3BHUAWEI%3BDUB-LX1%29")
                        .addHeader("content-type", "application/json")
                        .setConnectTimeout(30000)
                        .setReadTimeout(30000),
                null
        );

        return okResponse;
    }

    public OkResponse fetch(String url, Map<String, String> additionalHeaders) throws Exception {
        OkRequest okRequest = new OkRequest()
                .setMethod("GET")
                .setUrl(url)
                .addHeader("user-agent", "MTOPSDK%2F3.1.1.7+%28Android%3B8.1.0%3BHUAWEI%3BDUB-LX1%29")
                .addHeader("content-type", "application/json")
                .setConnectTimeout(30000)
                .setReadTimeout(30000);

        if (additionalHeaders != null && additionalHeaders.size() > 0) {
            for (String key : additionalHeaders.keySet()) {
                okRequest.addHeader(key, additionalHeaders.get(key));
            }
        }

        OkResponse okResponse;
        okResponse = OkHttpHelper.execute(
                okRequest,
                null
        );

        return okResponse;
    }
}
