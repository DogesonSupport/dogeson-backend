package com.highest.base.http.httpclient.extended;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

public class MyConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {

    // To be polite, set it small; if we use it, we will use less than a second
    // delay between subsequent fetches
    private static final int DEFAULT_KEEP_ALIVE_DURATION = 5000;

    @Override
    public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {

        HeaderElementIterator it = new BasicHeaderElementIterator(httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE));
        while (it.hasNext()) {
            HeaderElement he = it.nextElement();
            String param = he.getName();
            String value = he.getValue();
            if (value != null && param.equalsIgnoreCase("timeout")) {
                try {
                    return Long.parseLong(value) * 1000;
                } catch (NumberFormatException ex) {}
            }
        }

        return DEFAULT_KEEP_ALIVE_DURATION;
    }
}
