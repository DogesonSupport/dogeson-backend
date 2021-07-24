package com.highest.base.http;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@PropertySource("classpath:http.properties")
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfiguration {

    private List<String> ids;

    private List<String> urls;

    private List<Integer> timespans;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public List<Integer> getTimespans() {
        return timespans;
    }

    public void setTimespans(List<Integer> timespans) {
        this.timespans = timespans;
    }
}
