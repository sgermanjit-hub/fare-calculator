package com.littlepay.farecalculator.engine;

import java.math.BigDecimal;
import java.util.Optional;

public interface IFareRuleEngine {

    Optional<BigDecimal> computeCompletedTripFare(String startStopId, String endStopId);

    Optional<BigDecimal> computeIncompleteTripFare(String startStopId);

    Optional<BigDecimal> computeCancelledTripFare();
}
