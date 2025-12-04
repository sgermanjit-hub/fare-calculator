package com.littlepay.farecalculator.service;

import com.littlepay.farecalculator.BaseTest;
import com.littlepay.farecalculator.dto.Tap;
import com.littlepay.farecalculator.dto.TapType;
import com.littlepay.farecalculator.dto.Trip;
import com.littlepay.farecalculator.dto.TripStatus;
import com.littlepay.farecalculator.engine.IFareRuleEngine;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class TripServiceTest extends BaseTest {

    @Mock
    private IFareRuleEngine iFareRuleEngine;

    @InjectMocks
    TripService tripService;

    @Test
    void completedTrip_FareCalculatorTest(){
        Tap startTrip = tap(1,"PAN1","Company1","bus1", TapType.ON,"Stop1",String.valueOf(LocalDateTime.now().minusHours(1)));
        Tap endTrip = tap(2,"PAN1","Company1","bus1", TapType.OFF,"Stop2",String.valueOf(LocalDateTime.now()));
        BigDecimal fare = new BigDecimal("3.25");

        when(iFareRuleEngine.computeCompletedTripFare(anyString(),anyString())).thenReturn(Optional.of(fare));

        List<Trip> tripsList = tripService.computeTrips(List.of(startTrip, endTrip));
        assertThat(tripsList).hasSize(1);
        Trip trip = tripsList.getFirst();
        assertEquals(TripStatus.COMPLETED, trip.getTripStatus());
        assertEquals(endTrip.getStopId(), trip.getToStopId());
        assertEquals(startTrip.getStopId(), trip.getFromStopId());
        assertEquals(fare, trip.getChargeAmount());
        assertEquals(Duration.between(startTrip.getDateTimeUtc(), endTrip.getDateTimeUtc()).getSeconds(), trip.getDurationSecs());
        assertEquals(startTrip.getPan(), trip.getPan());
        assertEquals(startTrip.getCompanyId(), trip.getCompanyId());
        assertEquals(startTrip.getBusId(), trip.getBusId());
    }

    @Test
    void inCompleteTrip_FareCalculatorTest(){
        Tap startTrip = tap(1,"PAN1","Company1","bus1", TapType.ON,"Stop1",String.valueOf(LocalDateTime.now().minusHours(1)));
        BigDecimal fare = new BigDecimal("5.50");

        when(iFareRuleEngine.computeIncompleteTripFare(anyString())).thenReturn(Optional.of(fare));

        List<Trip> tripsList = tripService.computeTrips(List.of(startTrip));
        assertThat(tripsList).hasSize(1);
        Trip trip = tripsList.getFirst();
        assertEquals(TripStatus.INCOMPLETE, trip.getTripStatus());
        assertEquals(startTrip.getStopId(), trip.getFromStopId());
        assertEquals(null, trip.getToStopId());
        assertEquals(null, trip.getDurationSecs());
        assertEquals(fare, trip.getChargeAmount());
        assertEquals(startTrip.getPan(), trip.getPan());
        assertEquals(startTrip.getCompanyId(), trip.getCompanyId());
        assertEquals(startTrip.getBusId(), trip.getBusId());
    }

    @Test
    void cancelledTripSameStop_FareCalculatorTest(){
        LocalDateTime localDateTime = LocalDateTime.now();
        Tap startTrip = tap(1,"PAN1","Company1","bus1", TapType.ON,"Stop1",String.valueOf(localDateTime.minusHours(1)));
        Tap endTrip = tap(1,"PAN1","Company1","bus1", TapType.OFF,"Stop1",String.valueOf(localDateTime));
        BigDecimal fare = BigDecimal.ZERO;

        when(iFareRuleEngine.computeCancelledTripFare()).thenReturn(Optional.of(fare));

        List<Trip> tripsList = tripService.computeTrips(List.of(startTrip, endTrip));
        assertThat(tripsList).hasSize(1);
        Trip trip = tripsList.getFirst();
        assertEquals(TripStatus.CANCELLED, trip.getTripStatus());
        assertEquals(endTrip.getStopId(), trip.getToStopId());
        assertEquals(startTrip.getStopId(), trip.getFromStopId());
        assertEquals(fare, trip.getChargeAmount());
        assertEquals(Duration.between(startTrip.getDateTimeUtc(), endTrip.getDateTimeUtc()).getSeconds(), trip.getDurationSecs());
        assertEquals(startTrip.getPan(), trip.getPan());
        assertEquals(startTrip.getCompanyId(), trip.getCompanyId());
        assertEquals(startTrip.getBusId(), trip.getBusId());
    }

    @Test
    void orphanOffTrip_FareCalculatorTestIgnored(){
        Tap startTrip = tap(1,"PAN1","Company1","bus1", TapType.OFF,"Stop1",String.valueOf(LocalDateTime.now().minusHours(1)));

        List<Trip> tripsList = tripService.computeTrips(List.of(startTrip));
        assertThat(tripsList.isEmpty());
    }

    @Test
    void doubleTapOnTrip_FareCalculatorTest(){
        Tap startTrip = tap(1,"PAN1","Company1","bus1", TapType.ON,"Stop1",String.valueOf(LocalDateTime.now().minusHours(1)));
        Tap startTrip2 = tap(2,"PAN1","Company1","bus2", TapType.ON,"Stop2",String.valueOf(LocalDateTime.now().minusMinutes(30)));

        Tap endTrip = tap(3,"PAN1","Company1","bus2", TapType.OFF,"Stop3",String.valueOf(LocalDateTime.now()));
        BigDecimal fare = new BigDecimal("3.25");
        BigDecimal fareIncomplete = new BigDecimal("5.50");
        when(iFareRuleEngine.computeCompletedTripFare(anyString(),anyString())).thenReturn(Optional.of(fare));
        when(iFareRuleEngine.computeIncompleteTripFare(anyString())).thenReturn(Optional.of(fareIncomplete));

        List<Trip> tripsList = tripService.computeTrips(List.of(startTrip, startTrip2, endTrip));
        assertThat(tripsList).hasSize(2);
        Trip tripComplete = tripsList.getFirst();
        Trip tripIncomplete = tripsList.get(1);

        assertEquals(TripStatus.COMPLETED, tripComplete.getTripStatus());
        assertEquals(TripStatus.INCOMPLETE, tripIncomplete.getTripStatus());

        assertEquals(endTrip.getStopId(), tripComplete.getToStopId());
        assertEquals(startTrip2.getStopId(), tripComplete.getFromStopId());
        assertEquals(fare, tripComplete.getChargeAmount());
        assertEquals(Duration.between(startTrip2.getDateTimeUtc(), endTrip.getDateTimeUtc()).getSeconds(), tripComplete.getDurationSecs());
        assertEquals(startTrip2.getPan(), tripComplete.getPan());
        assertEquals(startTrip2.getCompanyId(), tripComplete.getCompanyId());
        assertEquals(startTrip2.getBusId(), tripComplete.getBusId());
    }

    @Test
    void engineReturningEmptyOptional_FareCalculatorTest(){
        Tap startTrip = tap(1,"PAN1","Company1","bus1", TapType.ON,"Stop1",String.valueOf(LocalDateTime.now().minusHours(1)));
        Tap endTrip = tap(2,"PAN1","Company1","bus1", TapType.OFF,"Stop2",String.valueOf(LocalDateTime.now()));

        when(iFareRuleEngine.computeCompletedTripFare(anyString(),anyString())).thenReturn(Optional.empty());

        List<Trip> tripsList = tripService.computeTrips(List.of(startTrip, endTrip));
        assertThat(tripsList.isEmpty());
    }
}



