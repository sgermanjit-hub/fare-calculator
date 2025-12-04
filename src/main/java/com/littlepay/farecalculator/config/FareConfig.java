package com.littlepay.farecalculator.config;

import com.littlepay.farecalculator.utils.NormalizationUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class FareConfig {

    /**
     * Configuring bean of all the fare rules containing stops
     * and price in fares.yml and loading in memory via bean
     * */
    @Bean
    public Map<Set<String>, BigDecimal> fareRules(FareProperties fareProperties, NormalizationUtils normalizationUtils) {
        //1. Load config and create Map with stops and price
        Map<Set<String>, BigDecimal> fareRules = fareProperties.getRules()
                .stream()
                .collect(Collectors.toMap(
                        rule -> Set.of(
                                normalizationUtils.normalize(rule.getFrom()),
                                normalizationUtils.normalize(rule.getTo())
                        ),
                        FareRule::getPrice
                ));
        //2. Make map immutable so that it can never be changed
        return Map.copyOf(fareRules);
    }

    /**
     * Set containing all stops provided in configuration to check for valid stop validation
     * */
    @Bean
    public Set<String> allowedStops(Map<Set<String>, BigDecimal> fareRules) {
        //Store all stops configured in the system
        return fareRules.keySet().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toUnmodifiableSet());
    }
}
