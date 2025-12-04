package com.littlepay.farecalculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlepay.farecalculator.model.request.TripProcessRequest;
import com.littlepay.farecalculator.service.TripService;
import com.littlepay.farecalculator.utils.CsvTapsReader;
import com.littlepay.farecalculator.utils.CsvTripsWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TripControllerIT {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    CsvTapsReader csvTapsReader;

    @Autowired
    CsvTripsWriter csvTripsWriter;

    @Autowired
    TripService tripService;

    @Test
    void endToEnd_validInput_producesTripsCsv() throws Exception {
        Path input = Files.createTempFile("taps1", ".csv");
        Files.writeString(input, """
                ID,DateTimeUTC,TapType,StopId,CompanyId,BusID,PAN
                1,22-01-2023 13:00:00,ON,Stop1,Company1,Bus37,5500005555555559
                2,22-01-2023 13:05:00,OFF,Stop2,Company1,Bus37,5500005555555559
                """);

        Path output = Files.createTempFile("trips1", ".csv");

        TripProcessRequest request = new TripProcessRequest();
        request.setInputFile(input.toString());
        request.setOutputFile(output.toString());

        mockMvc.perform(post("/trips/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk());

        List<String> lines = Files.readAllLines(output);
        assertThat(lines).hasSize(2); // header + one row
    }

    @Test
    void validationError_whenInputAndOutputMissing_shouldReturnBadRequest() throws Exception {
        //1. Request with fields inputFile/outputFile both are null
        TripProcessRequest request = new TripProcessRequest();

        mockMvc.perform(post("/trips/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Validation Failed"))
                .andExpect(jsonPath("$.details.inputFile", notNullValue()))
                .andExpect(jsonPath("$.details.outputFile", notNullValue()));
    }

    @Test
    void csvParsingError_whenInputFileDoesNotExist_shouldReturnBadRequest() throws Exception {
        Path output = Files.createTempFile("trips", ".csv");

        TripProcessRequest request = new TripProcessRequest();
        request.setInputFile("abc.csv");
        request.setOutputFile(output.toString());

        mockMvc.perform(post("/trips/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", containsString("CSV Parsing")));
    }
}
