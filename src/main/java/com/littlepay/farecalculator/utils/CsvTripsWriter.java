package com.littlepay.farecalculator.utils;

import com.littlepay.farecalculator.dto.Trip;
import com.littlepay.farecalculator.exception.TripsWriterFailureException;
import com.littlepay.farecalculator.mapper.TripMapper;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class CsvTripsWriter {

    private final TripMapper tripMapper;

    public CsvTripsWriter(TripMapper tripMapper) {
        this.tripMapper = tripMapper;
    }

    public void writeCsv(String path, List<Trip> trips) {
        //1. Write output to Csv file
        try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
            writer.writeNext(getHeaders());

            for (Trip trip : trips) {
                writer.writeNext(tripMapper.getRow(trip));
            }
            log.info("Trips created successfully to {}", path);
        } catch (IOException ioException) {
            throw new TripsWriterFailureException(String.format("Failed to write trips to path: %s , %s", path, ioException.getMessage()));
        }

    }

    private String[] getHeaders() {
        return new String[]{
                "Started",
                "Finished",
                "DurationSecs",
                "FromStopId",
                "ToStopId",
                "ChargeAmount",
                "CompanyId",
                "BusId",
                "PAN",
                "Status"
        };
    }

}
