package com.littlepay.farecalculator.mapper;

import com.littlepay.farecalculator.dto.Trip;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TripMapper {
    private final DateTimeFormatter dateTimeFormatter;

    public TripMapper(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public String[] getRow(Trip trip) {
        return new String[]{
                formatDate(trip.getStarted()),
                formatDate(trip.getFinished()),
                String.valueOf(trip.getDurationSecs()),
                trip.getFromStopId(),
                trip.getToStopId() == null ? "" : trip.getToStopId(),
                formatAmount(trip.getChargeAmount()),
                trip.getCompanyId(),
                trip.getBusId(),
                trip.getPan(),
                trip.getTripStatus().name()
        };
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "" : date.format(dateTimeFormatter);
    }

    private String formatAmount(BigDecimal chargeAmount) {
        if (chargeAmount == null) {
            return "";
        }
        return "$" + chargeAmount.toPlainString();
    }
}
