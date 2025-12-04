package com.littlepay.farecalculator.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

public interface IFareRuleRepository {

    Optional<BigDecimal> findFareForCompletedTrip(String fromStopId, String toStopId);

    Optional<BigDecimal> findMaxFareFromStop(String fromStopId);

    Set<String> findAllStops();
}

