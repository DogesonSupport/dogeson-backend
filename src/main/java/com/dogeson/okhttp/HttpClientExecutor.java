package com.dogeson.okhttp;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class HttpClientExecutor {

    private OkRequest request;
    private CookieStore cookieStore;

    public HttpClientExecutor(OkRequest request, CookieStore cookieStore) {
        this.request = request;
        this.cookieStore = cookieStore;
    }

    public OkResponse execute() {
        CookieStore cookieStore = getCookieStoreFromPool();

        try {
            URL url = new URL(request.getUrl());
            URLConnection openConnection = url.openConnection();

            HttpURLConnection httpURLConnection = (HttpURLConnection) openConnection;

            httpURLConnection.setConnectTimeout(request.getConnectTimeout());
            httpURLConnection.setReadTimeout(request.getReadTimeout());
            httpURLConnection.setRequestMethod(request.getMethod());

            for (String header : request.getHeaders().keySet()) {
                httpURLConnection.setRequestProperty(header, request.getHeaders().get(header));
            }

            if (cookieStore != null) {
                List<Cookie> cookies = cookieStore.getCookies();
                String cookieStr = "";
                for (Cookie cookie : cookies) {
                    cookieStr += cookie.getName() + "=" + cookie.getValue() + ";";
                }
                httpURLConnection.setRequestProperty("Cookie", cookieStr);
            }

            if (request.getMethod().toUpperCase().equals("POST")) {
                httpURLConnection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
                outputStreamWriter.write(request.getBody());
                outputStreamWriter.flush();
            }

            InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }

            OkResponse response = new OkResponse()
                    .setUrl(request.getUrl())
                    .setStatusCode(httpURLConnection.getResponseCode())
                    .setResponseBody(sb.toString());

            for (Map.Entry<String, List<String>> entry : httpURLConnection.getHeaderFields().entrySet()) {
                response.addHeader(entry.getKey(), httpURLConnection.getHeaderField(entry.getKey()));
            }

            return response;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private CookieStore getCookieStoreFromPool() {
        if (this.cookieStore != null) {
            try {
                URI uri = new URI(request.getUrl());
                String host = uri.getHost();

                CookieStore newCookieStore = new BasicCookieStore();

                List<Cookie> cookies = this.cookieStore.getCookies();
                for (Cookie cookie : cookies) {
                    Cookie cloneCookie = cookie;
                    if (cookie instanceof BasicClientCookie) {
                        BasicClientCookie basicClientCookie = (BasicClientCookie) cookie;
                        if (host.toLowerCase().indexOf(basicClientCookie.getDomain()) >= 0) {
                            cloneCookie = (Cookie) basicClientCookie.clone();
                            ((BasicClientCookie)cloneCookie).setDomain(host);

                        } else {
                            continue;
                        }
                    }
                    newCookieStore.addCookie(cloneCookie);
                }

                return newCookieStore;

            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        return cookieStore;
    }
}
