package com.dogeson.entity;

import lombok.Data;

@Data
public class TokenEntity {

    /**
     * token id in dextool
     */
    private String dexId;

    /**
     * symbol id in coingecko
     */
    private String geckoId;

    private String name;

    private String symbol;

    private String contractAddress;
}
