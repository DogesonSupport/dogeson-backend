package com.highest.base.http;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

public class HttpConfiguration {

    private boolean inspect = false;

    // 连接请求超时毫秒数
    private int connectionRequestTimeout = 0;

    // 连接超时毫秒数
    private int connectTimeout = 10000;

    // 读取超时毫秒数
    private int socketTimeout = 15000;

    // soKeepAlive
    private boolean keepAlive = true;

    // 连接池最大连接数
    private int maxConnectionsTotal = 12000;

    // 每个路由最大连接数
    private int maxConnectionsPerRoute = 6000;

    // 最大重试请求次数
    private int maxRequestRetryCount = 1;

    public boolean isInspect() {
        return inspect;
    }

    public void setInspect(boolean inspect) {
        this.inspect = inspect;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public int getMaxConnectionsTotal() {
        return maxConnectionsTotal;
    }

    public void setMaxConnectionsTotal(int maxConnectionsTotal) {
        this.maxConnectionsTotal = maxConnectionsTotal;
    }

    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    public int getMaxRequestRetryCount() {
        return maxRequestRetryCount;
    }

    public void setMaxRequestRetryCount(int maxRequestRetryCount) {
        this.maxRequestRetryCount = maxRequestRetryCount;
    }
}
