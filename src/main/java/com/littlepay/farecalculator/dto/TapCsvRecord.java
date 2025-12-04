package com.littlepay.farecalculator.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class TapCsvRecord {
    @CsvBindByName(column = "ID", required = true)
    private long id;
    @CsvBindByName(column = "DateTimeUTC", required = true)
    private String dateTimeUtc;
    @CsvBindByName(column = "TapType", required = true)
    private String tapType;
    @CsvBindByName(column = "StopId", required = true)
    private String stopId;
    @CsvBindByName(column = "CompanyId", required = true)
    private String companyId;
    @CsvBindByName(column = "BusId", required = true)
    private String busId;
    @CsvBindByName(column = "PAN", required = true)
    private String pan;
}
