package com.highest.base.http.httpclient.extended;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;

public class MyHttpRequestRetryHandler implements HttpRequestRetryHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private int maxRetryCount = 3;

    public MyHttpRequestRetryHandler() {
        this(3);
    }

    public MyHttpRequestRetryHandler(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext httpContext) {
        if (logger.isTraceEnabled()) {
            logger.trace("Decide about retry #" + executionCount + " for exception " + exception.getMessage());
        }

        if (executionCount >= maxRetryCount) {
            // Do not retry if over max retry count
            return false;
        } else if (exception instanceof NoHttpResponseException) {
            // Retry if the server dropped connection on us
            return true;
        } else if (exception instanceof SSLHandshakeException) {
            // Do not retry on SSL handshake exception
            return false;
        }

        HttpClientContext clientContext = HttpClientContext.adapt(httpContext);
        HttpRequest request = clientContext.getRequest();

        if (this.handleAsIdempotent(request)) {
            return true;
        }

        return false;
    }

    private boolean handleAsIdempotent(HttpRequest httpRequest) {
        boolean idempotent = !(httpRequest instanceof HttpEntityEnclosingRequest);
        // Retry if the request is considered idempotent
        return idempotent;
    }
}
