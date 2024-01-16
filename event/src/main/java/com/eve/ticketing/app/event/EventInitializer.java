package com.eve.ticketing.app.event;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventInitializer implements CommandLineRunner {

    private static final long HOUR = 60 * 60 * 1000;

    private static final long DAY = 24 * HOUR;

    private static final long YEAR = 365 * DAY;

    private final EventRepository eventRepository;

    @Override
    public void run(String... args) {
        log.info("Application has started generating \"Event\" data");
        List<Event> eventList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            eventList.add(createEvent());
        }
        eventRepository.saveAll(eventList);
        log.info("Application has generated 100 records of \"Event\" data");
    }

    private Event createEvent() {
        Faker faker = new Faker();
        Date date = faker.date().between(new Date((System.currentTimeMillis() - 2 * YEAR)), new Date((System.currentTimeMillis() + 2 * YEAR)));
        return Event.builder()
                .name(capitalize(String.join(" ", faker.lorem().words(3))))
                .description(String.join(" ", faker.lorem().sentences(5)))
                .unitPrice(BigDecimal.valueOf(faker.number().numberBetween(100, 1000)))
                .currency("$")
                .startAt(date)
                .endAt(new Date(date.getTime() + 2 * HOUR))
                .country(faker.address().country())
                .city(faker.address().city())
                .street(faker.address().streetAddress())
                .postalCode(faker.address().zipCode())
                .build();
    }

    private String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}
