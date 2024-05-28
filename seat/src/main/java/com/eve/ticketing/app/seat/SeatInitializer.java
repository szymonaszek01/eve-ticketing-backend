package com.eve.ticketing.app.seat;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class SeatInitializer implements CommandLineRunner {

    private final SeatRepository seatRepository;

    @Override
    public void run(String... args) {
        log.info("Application has started generating \"Seat\" data");
        Faker faker = new Faker();
        List<String> sectorList = Arrays.asList("A", "B", "C", "D");
        List<Seat> seatList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 25; k++) {
                    seatList.add(Seat.builder()
                            .sector("Sector " + sectorList.get(i))
                            .row(j + 1)
                            .number(k + 1)
                            .eventId(1L)
                            .occupied(faker.number().numberBetween(0, 2) == 1)
                            .build());
                }
            }
        }
        seatRepository.saveAll(seatList);
        log.info("Application has generated 100 records of \"Seat\" data");
    }
}
