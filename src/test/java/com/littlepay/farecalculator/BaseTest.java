package com.littlepay.farecalculator;

import com.littlepay.farecalculator.dto.Tap;
import com.littlepay.farecalculator.dto.TapType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class BaseTest {

    protected Tap tap(long id, String pan, String company, String bus, TapType type,
                      String stop, String time) {
        return Tap.builder()
                .id(id)
                .pan(pan)
                .companyId(company)
                .busId(bus)
                .tapType(type)
                .stopId(stop)
                .dateTimeUtc(LocalDateTime.parse(time))
                .build();
    }
}
