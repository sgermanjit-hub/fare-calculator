package com.littlepay.farecalculator.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class Trip {
    private LocalDateTime started;
    private LocalDateTime finished;
    private Long durationSecs;
    private String fromStopId;
    private String toStopId;
    private BigDecimal chargeAmount;
    private String companyId;
    private String busId;
    private String pan;
    private TripStatus tripStatus;
}
