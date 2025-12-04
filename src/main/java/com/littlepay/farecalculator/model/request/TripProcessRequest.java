package com.littlepay.farecalculator.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TripProcessRequest {
    @Schema(type = "string",
            example = "taps.csv",
            description = "Input file name")
    @NotBlank
    private String inputFile;

    @Schema(type = "string",
            example = "trips.csv",
            description = "Output file name")
    @NotBlank
    private String outputFile;
}
