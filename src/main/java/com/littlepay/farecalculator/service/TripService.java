package com.littlepay.farecalculator.service;

import com.littlepay.farecalculator.dto.Tap;
import com.littlepay.farecalculator.dto.TapType;
import com.littlepay.farecalculator.dto.Trip;
import com.littlepay.farecalculator.dto.TripStatus;
import com.littlepay.farecalculator.engine.IFareRuleEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
public class TripService {
    private final IFareRuleEngine fareRuleEngine;

    public TripService(IFareRuleEngine fareRuleEngine) {
        this.fareRuleEngine = fareRuleEngine;
    }

    public List<Trip> computeTrips(List<Tap> tapsList) {
        //1. Sort trips based on Date Time Utc (using UTC time for consistency)
        List<Tap> sortedTrips = tapsList.stream()
                .sorted(Comparator.comparing(Tap::getDateTimeUtc))
                .toList();

        //2. Create Map by grouping based on PAN, Company id and Bus id
        // Assumption: Tap in and tap out belong to same pan, company and bus
        Map<String, Tap> currentOnTripByGroup = new HashMap<>();
        List<Trip> tripsList = new ArrayList<>();

        //3. Iterate over sorted trips
        for (Tap tap : sortedTrips) {
            //4. Create Key for uniqueness of pan, company and bus for one trip
            String key = getGroupKey(tap);

            //5. Check if already trip is going on
            Tap openTrip = currentOnTripByGroup.get(key);
            if (tap.getTapType() == TapType.ON) {
                if (openTrip != null) {
                    // 5.1 Current Trip is Tap on and existing is also Tap on for same customer
                    // This means the previous trip is incomplete
                    log.warn("Double On for key {}, mark previous trip {} as incomplete", key, openTrip.getId());
                    buildIncompleteTrip(openTrip).ifPresent(tripsList::add);
                }
                currentOnTripByGroup.put(key, tap);
            } else {
                //6. We got tap off, but no Tap on trip exists
                // this means we dont know the starting point and hence its unfair to charge customer
                if (openTrip == null) {
                    log.warn("Orphan Off trip for key {}, OFF {} at {} is ignored", key, tap.getId(), tap.getDateTimeUtc());
                } else {
                    //7. Build Complete or Cancelled trip
                    buildCompleteOrCancelledTrip(openTrip, tap).ifPresent(tripsList::add);
                    currentOnTripByGroup.remove(key);
                }
            }
        }

        //8. All remaining trips left in map are incomplete
        //So we can directly count fare for those incomplete trips
        for (Tap openTrip : currentOnTripByGroup.values()) {
            buildIncompleteTrip(openTrip).ifPresent(tripsList::add);
        }
        return tripsList;
    }

    private String getGroupKey(Tap tap) {
        return String.join("_", tap.getPan(), tap.getCompanyId(), tap.getBusId());
    }

    private Optional<Trip> buildCompleteOrCancelledTrip(Tap openTrip, Tap currentTrip) {
        //1. Get trip status based on stop
        // If source stop is same as destination that means its a cancelled trip
        // If different, its a complete trip
        TripStatus tripStatus = openTrip.getStopId().equals(currentTrip.getStopId())
                ? TripStatus.CANCELLED
                : TripStatus.COMPLETED;

        //2. Calculate fare based on Cancelled/ Completed trips
        Optional<BigDecimal> fare = (tripStatus == TripStatus.CANCELLED)
                ? fareRuleEngine.computeCancelledTripFare()
                : fareRuleEngine.computeCompletedTripFare(openTrip.getStopId(), currentTrip.getStopId());

        //3. If no fare exists, simply skip the trip and move with rest of the trips processing
        // we cannot afford to throw exception for just one customer issue.
        if (fare.isEmpty()) {
            log.error("Skip completed trip for start trip id: {} , end trip id: {} as pricing failure occurred", openTrip.getId(), currentTrip.getId());
            return Optional.empty();
        }
        BigDecimal farePrice = fare.get();

        long tripDurationInSec = Duration.between(openTrip.getDateTimeUtc(), currentTrip.getDateTimeUtc()).getSeconds();
        //4. Create trip
        Trip trip = Trip.builder()
                .companyId(openTrip.getCompanyId())
                .busId(openTrip.getBusId())
                .pan(openTrip.getPan())
                .started(openTrip.getDateTimeUtc())
                .finished(currentTrip.getDateTimeUtc())
                .tripStatus(tripStatus)
                .chargeAmount(farePrice)
                .fromStopId(openTrip.getStopId())
                .toStopId(currentTrip.getStopId())
                .durationSecs(tripDurationInSec)
                .build();
        return Optional.of(trip);
    }

    private Optional<Trip> buildIncompleteTrip(Tap tapOn) {
        //1. Compute incomplete trip fare
        Optional<BigDecimal> incompleteTripFare = fareRuleEngine.computeIncompleteTripFare(tapOn.getStopId());
        if (incompleteTripFare.isEmpty()) {
            log.error("Skip Incomplete trip for start trip id: {} as pricing failure occurred", tapOn.getId());
            return Optional.empty();
        }
        //2. Create trip
        Trip trip = Trip.builder()
                .started(tapOn.getDateTimeUtc())
                .finished(null)
                .companyId(tapOn.getCompanyId())
                .busId(tapOn.getBusId())
                .pan(tapOn.getPan())
                .tripStatus(TripStatus.INCOMPLETE)
                .fromStopId(tapOn.getStopId())
                .durationSecs(null)
                .chargeAmount(incompleteTripFare.get())
                .build();
        return Optional.of(trip);
    }

}
