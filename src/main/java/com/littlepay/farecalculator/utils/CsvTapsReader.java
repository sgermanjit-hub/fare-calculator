package com.littlepay.farecalculator.utils;

import com.littlepay.farecalculator.dto.Tap;
import com.littlepay.farecalculator.dto.TapCsvRecord;
import com.littlepay.farecalculator.exception.CsvParsingException;
import com.littlepay.farecalculator.mapper.TapMapper;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CsvTapsReader {
    private final TapMapper tapMapper;

    public CsvTapsReader(TapMapper tapMapper) {
        this.tapMapper = tapMapper;
    }

    /**
     * This method is to parse the taps.csv and convert that into java models
     * */
    public List<Tap> readFile(String path) {
        //1. Header Mapping Strategy for creating DTO
        HeaderColumnNameMappingStrategy<TapCsvRecord> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(TapCsvRecord.class);

        //2. Reading Csv
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            CsvToBean<TapCsvRecord> csvBeanBinder = new CsvToBeanBuilder<TapCsvRecord>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .withThrowExceptions(false)
                    .build();

            //3. Wrapping in Java Csv pojo records
            List<TapCsvRecord> tapCsvRecordList = csvBeanBinder.parse();

            //4. Check any Csv format errors
            if (!csvBeanBinder.getCapturedExceptions().isEmpty()) {
                String errorMessage = csvBeanBinder.getCapturedExceptions().stream()
                        .map(e -> "line: " + e.getLineNumber() + " message: " + e.getMessage())
                        .collect(Collectors.joining(","));
                //4.1 Throw Exception if format is invalid
                throw new CsvParsingException("Error Parsing CSV " + errorMessage);
            }

            List<Tap> taps = new ArrayList<>();
            //5. Generate Tap model records
            for (TapCsvRecord tapCsvRecord : tapCsvRecordList) {
                try {
                    taps.add(tapMapper.convertToTap(tapCsvRecord));
                } catch (CsvParsingException | IllegalArgumentException e) {
                    // 5.1 Skip records if invalid as we can't impact other customers if one entry is invalid
                    // So processing should go on for rest of the tap entries.
                    log.error("Skipped Malformed record with id: {} , reason: {}", tapCsvRecord.getId(), e.getMessage());
                }
            }
            return taps;
        } catch (IOException e) {
            // Exception if File doesn't exists or any IO Failures.
            throw new CsvParsingException("CSV Parsing failed: " + e.getMessage());
        }
    }
}
