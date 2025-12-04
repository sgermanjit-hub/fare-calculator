package com.littlepay.farecalculator.utils;

import com.littlepay.farecalculator.exception.InvalidStopException;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class NormalizationUtils {
    public String normalize(String input) {
        if (input == null || input.trim().isBlank()) {
            throw new InvalidStopException("Invalid Stop, it must not be null");
        }
        return input.trim().toUpperCase(Locale.ROOT);
    }
}
