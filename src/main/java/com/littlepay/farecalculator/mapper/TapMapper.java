package com.littlepay.farecalculator.mapper;

import com.littlepay.farecalculator.dto.Tap;
import com.littlepay.farecalculator.dto.TapCsvRecord;
import com.littlepay.farecalculator.dto.TapType;
import com.littlepay.farecalculator.exception.CsvParsingException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TapMapper {
    private final DateTimeFormatter dateTimeFormatter;

    public TapMapper(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public Tap convertToTap(TapCsvRecord record) {
        return Tap.builder()
                .id(record.getId())
                .dateTimeUtc(LocalDateTime.parse(checkFieldValue(record.getDateTimeUtc(), "DateTimeUTC"), dateTimeFormatter))
                .tapType(TapType.valueOf(checkFieldValue(record.getTapType(), "TapType")))
                .stopId(checkFieldValue(record.getStopId(), "StopId"))
                .companyId(checkFieldValue(record.getCompanyId(), "CompanyId"))
                .busId(checkFieldValue(record.getBusId(), "BusId"))
                .pan(checkFieldValue(record.getPan(), "PAN"))
                .build();
    }

    private String checkFieldValue(String input, String fieldName) {
        if (input == null || input.isBlank()) {
            throw new CsvParsingException("CSV Field value must not be null or blank, field: " + fieldName);
        }
        return input.trim();
    }
}
