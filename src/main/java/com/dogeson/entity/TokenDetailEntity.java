package com.dogeson.entity;

import lombok.Data;

@Data
public class TokenDetailEntity extends TokenEntity {

    private String reddit;

    private String slack;

    private String twitter;

    private String facebook;

    private String telegram;

    private String website;

    private String iconSmall;

    private String iconLarge;

    private String iconThumb;

    private double price;

    private double priceChange24H;

    private double volumne24H;

    private double liquidity;

    private long totalSupply;

    private double marketCap;

    private double bnbLPHoldings;

    private double bnbLPHoldingsUSD;

    private long transactions;

    private long holders;
}
