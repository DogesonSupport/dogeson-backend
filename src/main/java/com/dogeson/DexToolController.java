package com.dogeson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dogeson.entity.Bar;
import com.dogeson.entity.TokenDetailEntity;
import com.dogeson.entity.TokenEntity;
import com.dogeson.okhttp.OkResponse;
import com.dogeson.utils.SelfExpiringHashMap;
import com.dogeson.utils.SelfExpiringMap;
import com.highest.base.http.HttpConfiguration;
import org.apache.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v1.0/dogeson")
public class DexToolController extends BaseController {

    private final HttpConfiguration httpConfiguration;

    private Map<String, DexAgent> dexAgents = new HashMap<>();

    public DexToolController() {
        httpConfiguration = new HttpConfiguration();
    }

    @Scheduled(cron = "0 * * ? * *") // every minute
    public void autoUpdateDexAgent() {
        List<String> removeKeys = new ArrayList<>();
        if (dexAgents.size() > 0) {
            for (String dexId : dexAgents.keySet()) {
                DexAgent dexAgent = dexAgents.get(dexId);
                if (!dexAgent.updateAuth()) {
                    removeKeys.add(dexId);
                }
            }
        }
        for (String dexId : removeKeys) {
            dexAgents.remove(dexId);
        }
    }

    @GetMapping("/hot")
    public R getHotTokens() {
        try {
            String url = "https://www.dextools.io/chain-bsc/api/dashboard/pancakeswap/hot";

            OkResponse okResponse = fetch(url);

            if (okResponse == null || okResponse.getStatusCode() != HttpStatus.SC_OK) {
                System.out.println(okResponse.getStatusCode());
                return R.error();
            }

            String resp = okResponse.getResponseBody();
            JSONArray root = JSON.parseArray(resp);

            List<TokenEntity> tokenEntityList = new ArrayList<>();
            for (Object jsonToken : root) {
                JSONObject jsonObjToken = (JSONObject) jsonToken;
                if (!jsonObjToken.containsKey("id") || !jsonObjToken.containsKey("tokenIndex"))
                    continue;

                int tokenIndex = (int) jsonObjToken.getIntValue("tokenIndex");
                JSONObject jsonTokenInfo = (JSONObject) jsonObjToken.getJSONObject("token" + tokenIndex);
                if (jsonTokenInfo == null)
                    continue;

                String name = jsonTokenInfo.getString("name");
                String symbol = jsonTokenInfo.getString("symbol");
                String contractAddress = jsonTokenInfo.getString("id");

                TokenEntity tokenEntity = new TokenEntity();
                tokenEntity.setDexId((String) jsonObjToken.getString("id"));
                tokenEntity.setName(name);
                tokenEntity.setSymbol(symbol);
                tokenEntity.setContractAddress(contractAddress);
                tokenEntityList.add(tokenEntity);
            }

            return R.ok().put("tokens", tokenEntityList);

        } catch (Exception ex) {
            ex.printStackTrace();
            return R.error();
        }
    }

    private boolean getContractInfo(String contractAddress, TokenDetailEntity tokenDetailEntity) throws Exception {
        String url = "https://api.coingecko.com/api/v3/coins/binance-smart-chain/contract/" + contractAddress;
        OkResponse okResponse = fetch(url);

        if (okResponse == null || okResponse.getStatusCode() != HttpStatus.SC_OK) {
            return false;
        }

        String resp = okResponse.getResponseBody();
        JSONObject root = JSON.parseObject(resp);

        String geckoId = root.getString("id");
        tokenDetailEntity.setGeckoId(geckoId);
        String name = root.getString("name");
        tokenDetailEntity.setName(name);
        String symbol = root.getString("symbol");
        tokenDetailEntity.setSymbol(symbol);
        if (name == null || name.length() < 1) {
            tokenDetailEntity.setName(symbol);
        }

        tokenDetailEntity.setContractAddress(contractAddress);

        // todo
        tokenDetailEntity.setReddit("");
        tokenDetailEntity.setSlack("");
        tokenDetailEntity.setTwitter("");
        tokenDetailEntity.setTelegram("");
        tokenDetailEntity.setWebsite("");

        JSONObject jsonImage = root.getJSONObject("image");
        String thumb = jsonImage.getString("thumb");
        String small = jsonImage.getString("small");
        String large = jsonImage.getString("large");

        tokenDetailEntity.setIconSmall(small);
        tokenDetailEntity.setIconThumb(thumb);
        tokenDetailEntity.setIconLarge(large);

        // todo
        tokenDetailEntity.setPrice(0);

        JSONObject jsonMarketData = root.getJSONObject("market_data");
        tokenDetailEntity.setPriceChange24H(jsonMarketData.getDouble("price_change_24h"));
        tokenDetailEntity.setVolumne24H(0); // todo
        tokenDetailEntity.setLiquidity(0); // todo
        tokenDetailEntity.setTotalSupply(jsonMarketData.getLong("total_supply"));
        tokenDetailEntity.setMarketCap(tokenDetailEntity.getTotalSupply() * tokenDetailEntity.getPrice());

        return true;
    }

    private boolean getTokenPrice(TokenDetailEntity tokenDetailEntity) throws Exception {
        String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + tokenDetailEntity.getGeckoId() + "&vs_currencies=usd&include_24hr_vol=true&include_24hr_change=true";
        OkResponse okResponse = fetch(url);

        if (okResponse == null || okResponse.getStatusCode() != HttpStatus.SC_OK) {
            return false;
        }

        String resp = okResponse.getResponseBody();
        JSONObject root = JSON.parseObject(resp);

        JSONObject jsonToken = root.getJSONObject(tokenDetailEntity.getGeckoId());
        tokenDetailEntity.setPrice(jsonToken.getDouble("usd"));
        tokenDetailEntity.setVolumne24H(jsonToken.getDouble("usd_24h_vol"));
        tokenDetailEntity.setPriceChange24H(jsonToken.getDouble("usd_24h_change"));
        return true;
    }

    private boolean getTokenMedia(TokenDetailEntity tokenDetailEntity) throws Exception {
        String url = "https://www.dextools.io/chain-bsc/api/pancakeswap/token?address=" + tokenDetailEntity.getContractAddress();
        OkResponse okResponse = fetch(url);

        if (okResponse == null || okResponse.getStatusCode() != HttpStatus.SC_OK) {
            return false;
        }

        String resp = okResponse.getResponseBody();
        JSONObject root = JSON.parseObject(resp);
        JSONArray jsonResults = root.getJSONArray("result");
        if (jsonResults.size() < 1) {
            return false;
        }

        JSONObject jsonResult = jsonResults.getJSONObject(0);
        String website = jsonResult.getString("website");
        tokenDetailEntity.setWebsite(website);
        String reddit = jsonResult.getString("reddit");
        tokenDetailEntity.setReddit(reddit);
        String slack = jsonResult.getString("slack");
        tokenDetailEntity.setSlack(slack);
        String facebook = jsonResult.getString("facebook");
        tokenDetailEntity.setFacebook(facebook);
        String twitter = jsonResult.getString("twitter");
        tokenDetailEntity.setTwitter(twitter);
        String telegram = jsonResult.getString("telegram");
        tokenDetailEntity.setTelegram(telegram);

        return true;
    }

    private boolean getDexInfo(String dexInfo, TokenDetailEntity tokenDetailEntity) throws Exception {
        String url = "https://www.dextools.io/chain-bsc/api/pancakeswap/poolx?pairSelected=" + dexInfo;
        OkResponse okResponse = fetch(url);

        if (okResponse == null || okResponse.getStatusCode() != HttpStatus.SC_OK) {
            return false;
        }

        String resp = okResponse.getResponseBody();
        JSONObject root = JSON.parseObject(resp);
        JSONObject data = root.getJSONObject("data");
        JSONObject pair = data.getJSONObject("pair");

        JSONObject jsonInfo = pair.getJSONObject("info");
        String contractAddress = jsonInfo.getString("address");
        int holders = jsonInfo.getIntValue("holders");
        int txCount = pair.getIntValue("txCount");

        tokenDetailEntity.setContractAddress(contractAddress);
        tokenDetailEntity.setHolders(holders);
        tokenDetailEntity.setTransactions(txCount);

        return true;
    }

    @GetMapping("/info/dex/{dexId}")
    public R getTokenDexInfo(@PathVariable String dexId) {
        try {
            TokenDetailEntity tokenDetailEntity = new TokenDetailEntity();

            tokenDetailEntity.setDexId(dexId);

            if (getDexInfo(dexId, tokenDetailEntity) &&
                    getContractInfo(tokenDetailEntity.getContractAddress(), tokenDetailEntity) &&
                    getTokenPrice(tokenDetailEntity) &&
                    getTokenMedia(tokenDetailEntity)) {
                tokenDetailEntity.setMarketCap(tokenDetailEntity.getTotalSupply() * tokenDetailEntity.getPrice());
                return R.ok().put("token", tokenDetailEntity);
            }

            return R.error();

        } catch (Exception ex) {
            ex.printStackTrace();
            return R.error();
        }
    }

    @GetMapping("/info/contract/{contractAddress}")
    public R getTokenInfo(@PathVariable String contractAddress) {
        try {
            TokenDetailEntity tokenDetailEntity = new TokenDetailEntity();

            if (getContractInfo(contractAddress, tokenDetailEntity) &&
                    getTokenPrice(tokenDetailEntity) &&
                    getTokenMedia(tokenDetailEntity)) {

                tokenDetailEntity.setMarketCap(tokenDetailEntity.getTotalSupply() * tokenDetailEntity.getPrice());
                return R.ok().put("token", tokenDetailEntity);
            }

            return R.error();

        } catch (Exception ex) {
            ex.printStackTrace();
            return R.error();
        }
    }

    @GetMapping("/historical")
    public R getHistorical(@RequestParam(name = "dexId") String dexId,
                           @RequestParam(name = "span") String span) {
        try {
            if (!dexAgents.containsKey(dexId)) {
                DexAgent dexAgent1 = new DexAgent(httpConfiguration, dexId);
                if (dexAgent1.isAuthorized())
                    dexAgents.put(dexId, dexAgent1);
            }

            DexAgent dexAgent = dexAgents.get(dexId);
            if (dexAgent == null) {
                return R.error();
            }

            List<Bar> bars = dexAgent.getHistoricalData(span);

            return R.ok().put("bars", bars);

        } catch (Exception ex) {
            ex.printStackTrace();
            return R.error();
        }
    }
}
