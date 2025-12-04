package com.littlepay.farecalculator.repository.impl;

import com.littlepay.farecalculator.repository.IFareRuleRepository;
import com.littlepay.farecalculator.utils.NormalizationUtils;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class FareRuleRepositoryImpl implements IFareRuleRepository {

    private final Map<Set<String>, BigDecimal> fareRules;
    private final Set<String> allowedStops;

    public FareRuleRepositoryImpl(Map<Set<String>, BigDecimal> fareRules, Set<String> allowedStops, NormalizationUtils normalizationUtils) {
        this.fareRules = fareRules;
        this.allowedStops = allowedStops;
    }

    @Override
    public Optional<BigDecimal> findFareForCompletedTrip(String fromStopId, String toStopId) {
        //1. Retrieve fare price from Fare rule configs
        return Optional.ofNullable(fareRules.get(Set.of(fromStopId, toStopId)));
    }

    @Override
    public Optional<BigDecimal> findMaxFareFromStop(String fromStopId) {
        //1. Retrieve max fare for incomplete trip
        return fareRules.entrySet().stream()
                .filter(entry -> entry.getKey().contains(fromStopId))
                .map(Map.Entry::getValue)
                .max(Comparator.naturalOrder());
    }

    @Override
    public Set<String> findAllStops() {
        return allowedStops;
    }
}
