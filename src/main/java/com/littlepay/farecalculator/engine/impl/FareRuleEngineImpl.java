package com.littlepay.farecalculator.engine.impl;

import com.littlepay.farecalculator.engine.IFareRuleEngine;
import com.littlepay.farecalculator.repository.IFareRuleRepository;
import com.littlepay.farecalculator.utils.NormalizationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
public class FareRuleEngineImpl implements IFareRuleEngine {

    private final IFareRuleRepository iFareRuleRepository;
    private final NormalizationUtils normalizationUtils;

    public FareRuleEngineImpl(IFareRuleRepository iFareRuleRepository, NormalizationUtils normalizationUtils) {
        this.iFareRuleRepository = iFareRuleRepository;
        this.normalizationUtils = normalizationUtils;
    }

    @Override
    public Optional<BigDecimal> computeCompletedTripFare(String startStopId, String endStopId) {
        //1. Normalize Starting stop
        String fromStopId = normalizationUtils.normalize(startStopId);
        //2. Normalize Ending stop
        String toStopId = normalizationUtils.normalize(endStopId);
        //3. Check if valid stops in request
        if (isUnknownStop(fromStopId) || isUnknownStop(toStopId)) {
            //4. For invalid stops, just return empty so that record will be skipped.
            log.error("Unknown stop encountered in computeCompletedTripFare request, from: {} , to: {}", fromStopId, toStopId);
            return Optional.empty();
        }
        //4. If start stop is same as end stop
        // this means person didn't travel and hence don't charge
        if (fromStopId.equals(toStopId)) {
            return Optional.of(BigDecimal.ZERO);
        }

        //5. Compute fare based on fare rules for completed trip
        Optional<BigDecimal> fare = iFareRuleRepository.findFareForCompletedTrip(fromStopId, toStopId);

        //6. Return Empty if fare rule configuration for requested stops is missing.
        if (fare.isEmpty()) {
            log.error("No fare is configured between stops, from: {}, to: {}", fromStopId, toStopId);
            return Optional.empty();
        }
        return fare;
    }

    @Override
    public Optional<BigDecimal> computeIncompleteTripFare(String startStopId) {
        //1. Normalize starting stop
        String fromStopId = normalizationUtils.normalize(startStopId);
        //2. Check valid stop
        if (isUnknownStop(fromStopId)) {
            log.error("Unknown stop encountered in computeIncompleteTripFare request, from: {} ", fromStopId);
            return Optional.empty();
        }
        //3. Compute max fare as end stop doesn't exists - incomplete trip
        Optional<BigDecimal> maxFare = iFareRuleRepository.findMaxFareFromStop(fromStopId);
        if (maxFare.isEmpty()) {
            log.error("No max fare configured for stop {} ", fromStopId);
            return Optional.empty();
        }
        return maxFare;
    }

    @Override
    public Optional<BigDecimal> computeCancelledTripFare() {
        return Optional.of(BigDecimal.ZERO);
    }

    private boolean isUnknownStop(String stopId) {
        return !iFareRuleRepository.findAllStops().contains(stopId);
    }
}
