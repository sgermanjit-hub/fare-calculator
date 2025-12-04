package com.littlepay.farecalculator.controller;

import com.littlepay.farecalculator.dto.Tap;
import com.littlepay.farecalculator.dto.Trip;
import com.littlepay.farecalculator.model.request.TripProcessRequest;
import com.littlepay.farecalculator.service.TripService;
import com.littlepay.farecalculator.utils.CsvTapsReader;
import com.littlepay.farecalculator.utils.CsvTripsWriter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/trips")
public class TripController {

    private final CsvTapsReader csvTapsReader;
    private final TripService tripService;
    private final CsvTripsWriter csvTripsWriter;

    public TripController(CsvTapsReader csvTapsReader, TripService tripService, CsvTripsWriter csvTripsWriter) {
        this.csvTapsReader = csvTapsReader;
        this.tripService = tripService;
        this.csvTripsWriter = csvTripsWriter;
    }


    @PostMapping("/process")
    public ResponseEntity<String> processTrips(@RequestBody @Valid TripProcessRequest tripProcessRequest) {
        //1. Read Input file containing trips
        List<Tap> tapsList = csvTapsReader.readFile(tripProcessRequest.getInputFile());
        //2. Generate the trips combinations
        List<Trip> trips = tripService.computeTrips(tapsList);
        //3. Write computed trips in csv file.
        csvTripsWriter.writeCsv(tripProcessRequest.getOutputFile(), trips);
        return ResponseEntity.ok("Trips has been processed successfully.");
    }
}
