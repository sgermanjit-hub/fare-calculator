package com.littlepay.farecalculator.config;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareRule {
    private String from;
    private String to;
    private BigDecimal price;
}
