package com.littlepay.farecalculator.engine;

import com.littlepay.farecalculator.BaseTest;
import com.littlepay.farecalculator.engine.impl.FareRuleEngineImpl;
import com.littlepay.farecalculator.repository.IFareRuleRepository;
import com.littlepay.farecalculator.utils.NormalizationUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class FareEngineRuleImplTest extends BaseTest {

    @Mock
    IFareRuleRepository iFareRuleRepository;

    @Mock
    NormalizationUtils normalizationUtils;

    @InjectMocks
    FareRuleEngineImpl fareRuleEngineImpl;

    @Test
    void computeCompletedTripFare__shouldReturnFare() {
        BigDecimal fare = new BigDecimal("3.25");
        when(iFareRuleRepository.findAllStops()).thenReturn(Set.of("Stop1", "Stop2"));
        when(normalizationUtils.normalize("Stop1")).thenReturn("Stop1");
        when(normalizationUtils.normalize("Stop2")).thenReturn("Stop2");
        when(iFareRuleRepository.findFareForCompletedTrip("Stop1", "Stop2"))
                .thenReturn(Optional.of(fare));

        Optional<BigDecimal> result = fareRuleEngineImpl.computeCompletedTripFare("Stop1", "Stop2");

        assertThat(result).isPresent();
        BigDecimal fareResult = result.get();
        assertEquals(fare, fareResult);
    }

    @Test
    void computeCompletedTripFare_andUnknownStopWithResultEmpty() {
        when(iFareRuleRepository.findAllStops()).thenReturn(Set.of("Stop1", "Stop2"));
        when(normalizationUtils.normalize("Stop1")).thenReturn("Stop1");
        when(normalizationUtils.normalize("Stop3")).thenReturn("Stop3");

        Optional<BigDecimal> result = fareRuleEngineImpl.computeCompletedTripFare("Stop3", "Stop1");

        assertThat(result).isEmpty();
    }

    @Test
    void computeCompletedTripFare_sameStopWithResultZero() {
        BigDecimal fare = BigDecimal.ZERO;
        when(iFareRuleRepository.findAllStops()).thenReturn(Set.of("Stop1"));
        when(normalizationUtils.normalize("Stop1")).thenReturn("Stop1");

        var result = fareRuleEngineImpl.computeCompletedTripFare("Stop1", "Stop1");

        assertThat(result).isPresent();
        BigDecimal fareResult = result.get();
        assertEquals(fare, fareResult);
    }

    @Test
    void computeIncompleteTripFare_shouldReturnMaxFareForStop() {
        BigDecimal fare = new BigDecimal("5.50");
        when(normalizationUtils.normalize("Stop1")).thenReturn("Stop1");

        when(iFareRuleRepository.findAllStops()).thenReturn(Set.of("Stop1"));
        when(iFareRuleRepository.findMaxFareFromStop("Stop1"))
                .thenReturn(Optional.of(fare));

        Optional<BigDecimal> result = fareRuleEngineImpl.computeIncompleteTripFare("Stop1");

        assertThat(result).isPresent();
        BigDecimal fareResult = result.get();
        assertEquals(fare, fareResult);
    }

    @Test
    void computeIncompleteTripFare_noFareWithResultEmpty() {
        when(normalizationUtils.normalize("Stop1")).thenReturn("Stop1");

        when(iFareRuleRepository.findAllStops()).thenReturn(Set.of("Stop1"));
        when(iFareRuleRepository.findMaxFareFromStop("Stop1"))
                .thenReturn(Optional.empty());

        var result = fareRuleEngineImpl.computeIncompleteTripFare("Stop1");

        assertThat(result).isEmpty();
    }

    @Test
    void computeCancelledTripFare_WithResultZero() {
        assertEquals(Optional.of(BigDecimal.ZERO), fareRuleEngineImpl.computeCancelledTripFare());
    }
}
