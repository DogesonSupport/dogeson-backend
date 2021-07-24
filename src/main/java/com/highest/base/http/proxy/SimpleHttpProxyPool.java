package com.highest.base.http.proxy;

import com.highest.base.http.*;
import com.highest.base.http.httpclient.response.Response;
import com.highest.utils.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 国内代理IP
 */
@Component
@Qualifier("simpleHttpProxyPool")
public class SimpleHttpProxyPool extends HttpProxyPool {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProxyConfiguration proxyConfiguration;

    @Autowired
    private HttpConfiguration httpConfiguration;

    private boolean useProxy = false;

    /**
     * 最后获取代理IP的时间
     */
    private Map<String, Date> lastTimes = new HashMap<>();

    private Map<String, List<HttpProxy>> httpProxyMap = new HashMap<>();    //可用代理列表

    private ProxyStrategy proxyStrategy = new SequenceProxyStrategy();    //代理策略

    private HttpProxy lastHttpProxy = null;

    public boolean getUseProxy() {
        return useProxy;
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    @Override
    public HttpProxy getProxy(Request request) {
        if (!useProxy) {
            return null;
        }

        if (proxyConfiguration.getIds() == null) {
            return null;
        }

        List<HttpProxy> allHttpProxies = new ArrayList<>();

        int count = proxyConfiguration.getIds().size();
        for (int idx = 0; idx < count; idx++) {
            String id = proxyConfiguration.getIds().get(idx);
            String url = proxyConfiguration.getUrls().get(idx);
            int timespan = proxyConfiguration.getTimespans().get(idx);

            Date lastTime = lastTimes.get(id);
            Date now = new Date();
            if (lastTime == null || now.getTime() - lastTime.getTime() >= timespan) {
                List<HttpProxy> httpProxies = getHttpProxies(url);
                lastTimes.put(id, new Date());
                if (httpProxies == null)
                    httpProxyMap.remove(id);
                else
                    httpProxyMap.put(id, httpProxies);
            }

            List<HttpProxy> httpProxies = httpProxyMap.get(id);
            if (httpProxies != null) {
                allHttpProxies.addAll(httpProxies);
            }
        }

        HttpProxy httpProxy = proxyStrategy.getProxy(allHttpProxies);
//        while (lastHttpProxy != null && httpProxy != null && lastHttpProxy.getHost().compareTo(httpProxy.getHost()) == 0) {
//            httpProxy = proxyStrategy.getProxy(allHttpProxies);
//        }
        if (httpProxy != null) {
            logger.info("Proxy Host: " + httpProxy.getHost() + ", " + httpProxy.getPort());
        }
        lastHttpProxy = httpProxy;

        return httpProxy;
    }

    public List<HttpProxy> getHttpProxies(String url) {
        try {
            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setConnectionRequestTimeout(httpConfiguration.getConnectionRequestTimeout())
                            .setConnectTimeout(httpConfiguration.getConnectTimeout())
                            .setSocketTimeout(httpConfiguration.getSocketTimeout())
                            .setSoKeepAlive(httpConfiguration.isKeepAlive())
                            .setMaxConnTotal(httpConfiguration.getMaxConnectionsTotal())
                            .setMaxConnPerRoute(httpConfiguration.getMaxConnectionsPerRoute())
                            .setMaxRequestRetryCount(httpConfiguration.getMaxRequestRetryCount()),
                    new Request("GET", url, ResponseType.TEXT));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return null;
            }

            String respText = response.getResult();
            if (respText.indexOf(':') < 0) {
                return null;
            }

            List<HttpProxy> httpProxies = new ArrayList<>();

            String[] tokens = respText.split("\n");
            for (String token : tokens) {
                token = StringUtils.replace(token, "\n", "");

                String[] parts = token.split(":");
                if (parts.length < 2) {
                    continue;
                }
                String ip = StringUtils.replace(parts[0], "\n", "").replace("\r", "");
                parts[1] = StringUtils.replace(parts[1], "\n", "").replace("\r", "");

                if (!org.apache.commons.lang3.math.NumberUtils.isDigits(parts[1]))
                    continue;

                int port = NumberUtils.valueOf(NumberUtils.parseInt(parts[1]));

                if (StringUtils.isEmpty(ip) || port < 1) {
                    continue;
                }

                httpProxies.add(new HttpProxy(ip, port));
            }

            return httpProxies;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void returnProxy(Request request, int statusCode) {

    }
}
