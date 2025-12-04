package com.littlepay.farecalculator.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Tap {
    private long id;
    private LocalDateTime dateTimeUtc;
    private TapType tapType;
    private String stopId;
    private String companyId;
    private String busId;
    private String pan;
}
