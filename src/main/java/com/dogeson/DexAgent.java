package com.dogeson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dogeson.entity.Bar;
import com.dogeson.okhttp.OkHttpHelper;
import com.dogeson.okhttp.OkRequest;
import com.dogeson.okhttp.OkResponse;
import com.highest.base.http.*;
import com.highest.base.http.httpclient.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DexAgent {

    private final HttpConfiguration httpConfiguration;

    private final String dexId, referer;

    private String xAuth;

    public DexAgent(HttpConfiguration httpConfiguration, String dexId) {
        this.httpConfiguration = httpConfiguration;
        this.dexId = dexId;
        this.referer = "https://www.dextools.io/app/pancakeswap/pair-explorer/" + dexId;
        login();
    }
    
    public boolean isAuthorized() {
        return !StringUtils.isBlank(xAuth);
    }

    private boolean login() {
        xAuth = "";

        String url = "https://www.dextools.io/back/user/login";

        OkRequest okRequest = new OkRequest()
                .setMethod("POST")
                .setUrl(url)
                .addHeader("user-agent", "MTOPSDK%2F3.1.1.7+%28Android%3B8.1.0%3BHUAWEI%3BDUB-LX1%29")
                .addHeader("Content-Type", "application/json")
                .setConnectTimeout(30000)
                .setReadTimeout(30000)
                .addHeader("Authorization", "Bearer null")
                .addHeader("Referer", referer);

        okRequest.setBody("{\"id\":\"anyone\",\"password\":\"m4gkvkVTgDsVu3hFy05uD85aIQkCYJBBvMzEhDJDsBY=\"}");

        OkResponse okResponse = OkHttpHelper.execute(
                okRequest,
                null
        );

        if (okResponse == null || okResponse.getStatusCode() != HttpStatus.SC_OK) {
            return false;
        }

        for (String key : okResponse.getHeaders().keySet()) {
            if (key == null) {
                continue;
            }
            if (key.compareToIgnoreCase("X-Auth") == 0) {
                xAuth = okResponse.getHeaders().get(key);
            }
        }
        return true;
    }

    public boolean updateAuth() {
        if (StringUtils.isBlank(xAuth)) {
            return login();
        }

        String url = "https://www.dextools.io/meta?prop=version";

        Response<String> response = HttpHelper.execute(
                new SiteConfig()
                        .setConnectionRequestTimeout(httpConfiguration.getConnectionRequestTimeout())
                        .setConnectTimeout(httpConfiguration.getConnectTimeout())
                        .setSocketTimeout(httpConfiguration.getSocketTimeout())
                        .setSoKeepAlive(httpConfiguration.isKeepAlive())
                        .setMaxConnTotal(httpConfiguration.getMaxConnectionsTotal())
                        .setMaxConnPerRoute(httpConfiguration.getMaxConnectionsPerRoute())
                        .setMaxRequestRetryCount(httpConfiguration.getMaxRequestRetryCount())
                        .setUserAgent("MTOPSDK%2F3.1.1.7+%28Android%3B5.1.1%3Bsamsung%3BSM-J120F%29")
                        .setContentType("application/json")
                        .addHeader("Authorization", "Bearer " + xAuth)
                        .addHeader("Referer", referer),
                new Request("GET", url, ResponseType.TEXT),
                null, null);

        if (response.getStatusCode() != HttpStatus.SC_OK) {
            return false;
        }

        boolean found = true;
        for (Header header : response.getHeaders()) {
            if (header.getName().compareToIgnoreCase("X-Auth") == 0) {
                xAuth = header.getValue();
                found = true;
            }
        }
        if (!found) {
            xAuth = "";
        }
        return isAuthorized();
    }

    public List<Bar> getHistoricalData(String span) {
        Date now = new Date();
        Date timestamp = new Date(now.getYear(), now.getMonth(), now.getDate());

        if (span.compareToIgnoreCase("month") == 0) {
            timestamp = DateUtils.addDays(timestamp, -14);
        } else { // week
            timestamp = DateUtils.addDays(timestamp, -15);
        }

        String url = "https://www.dextools.io/chain-bsc/api/pancakeswap/history/candles?sym=usd&span=" + span + "&pair=" + dexId + "&ts=" + timestamp.getTime() + "&v=0";

        Response<String> response = HttpHelper.execute(
                new SiteConfig()
                        .setConnectionRequestTimeout(httpConfiguration.getConnectionRequestTimeout())
                        .setConnectTimeout(httpConfiguration.getConnectTimeout())
                        .setSocketTimeout(httpConfiguration.getSocketTimeout())
                        .setSoKeepAlive(httpConfiguration.isKeepAlive())
                        .setMaxConnTotal(httpConfiguration.getMaxConnectionsTotal())
                        .setMaxConnPerRoute(httpConfiguration.getMaxConnectionsPerRoute())
                        .setMaxRequestRetryCount(httpConfiguration.getMaxRequestRetryCount())
                        .setUserAgent("MTOPSDK%2F3.1.1.7+%28Android%3B5.1.1%3Bsamsung%3BSM-J120F%29")
                        .setContentType("application/json")
                        .addHeader("Authorization", "Bearer " + xAuth)
                        .addHeader("Referer", referer),
                new Request("GET", url, ResponseType.TEXT),
                null, null);

        if (response.getStatusCode() != HttpStatus.SC_OK) {
            return null;
        }

        String resp = response.getResult();
        JSONObject root = JSON.parseObject(resp);
        JSONObject jsonData = root.getJSONObject("data");
        if (jsonData == null) {
            return null;
        }
        JSONArray jsonCandles = jsonData.getJSONArray("candles");
        if (jsonCandles == null) {
            return null;
        }
        List<Bar> bars = new ArrayList<>();
        for (Object candle : jsonCandles) {
            JSONObject jsonCandle = (JSONObject) candle;
            long time = jsonCandle.getLong("time");
            double open = jsonCandle.getDouble("open");
            double high = jsonCandle.getDouble("high");
            double low = jsonCandle.getDouble("low");
            double close = jsonCandle.getDouble("close");
            double volume = jsonCandle.getDouble("volume");

            Bar bar = Bar.builder()
                    .time(time)
                    .open(open)
                    .high(high)
                    .low(low)
                    .close(close)
                    .volume(volume)
                    .build();
            bars.add(bar);
        }
        return bars;
    }
}
