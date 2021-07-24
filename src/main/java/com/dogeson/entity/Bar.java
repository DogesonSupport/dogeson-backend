package com.dogeson.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Bar {

    private long time;

    private double open;

    private double high;

    private double low;

    private double close;

    private double volume;
}
