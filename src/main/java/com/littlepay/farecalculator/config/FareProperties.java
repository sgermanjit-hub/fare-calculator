package com.littlepay.farecalculator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "fares")
public class FareProperties {
    private List<FareRule> rules;
}
