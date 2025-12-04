package com.littlepay.farecalculator;

import com.littlepay.farecalculator.config.FareProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FareProperties.class)
public class FareCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FareCalculatorApplication.class, args);
    }

}
