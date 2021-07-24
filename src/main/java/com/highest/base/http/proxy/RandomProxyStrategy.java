package com.highest.base.http.proxy;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Random;

/**
 * 随机获取代理的策略
 * Created by brucezee on 2017/1/20.
 */
public class RandomProxyStrategy implements ProxyStrategy {
    @Override
    public HttpProxy getProxy(List<HttpProxy> httpProxies) {
        if (CollectionUtils.isNotEmpty(httpProxies)) {
            HttpProxy httpProxy = httpProxies.get(new Random().nextInt(httpProxies.size()-1));
            System.out.println("PROXY HOST = " + httpProxy.getHost() + ":" + httpProxy.getPort());
            return httpProxy;
        }
        return null;
    }
}
