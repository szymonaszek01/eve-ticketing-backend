package com.eve.ticketing.app.event;

import com.eve.ticketing.app.event.dto.EventFilterDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Objects;

import static com.eve.ticketing.app.event.EventSpecification.*;

@Slf4j
@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    public Page<Event> getEventList(int page, int size, EventFilterDto eventFilterDto) {
        Specification<Event> eventSpecification = Specification.where(eventNameEqual(eventFilterDto.getName()))
                .and(eventUnitPriceBetween(eventFilterDto.getMinUnitPrice(), eventFilterDto.getMaxUnitPrice()))
                .and(eventStartAtBetween(eventFilterDto.getMinDate(), eventFilterDto.getMaxDate()))
                .and(eventEndAtBetween(eventFilterDto.getMinDate(), eventFilterDto.getMaxDate()))
                .and(eventCountryEqual(eventFilterDto.getCountry())).and(eventAddressEqual(eventFilterDto.getAddress()));
        Pageable pageable = PageRequest.of(page, size);

        return eventRepository.findAll(eventSpecification, pageable);
    }

    @Override
    public Event getEventById(long id) throws EventProcessingException {
        return eventRepository.findById(id).orElseThrow(() -> {
            log.error("Event (id=\"{}\") was not found", id);
            return new EventProcessingException("Event was not found - invalid event id");
        });
    }

    @Override
    @Transactional
    public void createEvent(Event event) throws EventProcessingException {
        try {
            eventRepository.save(event);
            log.info("Event (id=\"{}\") was created", event.getId());
        } catch (RuntimeException e) {
            log.error("Event was not created - {}", e.getMessage());
            throw new EventProcessingException("Event was not created - " + e.getMessage());
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void updateEvent(HashMap<String, Object> values) throws EventProcessingException {
        if (values == null) {
            log.error("Event was not updated - empty values");
            throw new EventProcessingException("Event was not updated - empty values");
        }

        Event event = getEventById(((Number) values.remove("id")).longValue());
        values.forEach((key, value) -> {
            String convertedKey = toCamelCase(key);
            try {
                Field field = event.getClass().getDeclaredField(convertedKey);
                field.setAccessible(true);
                boolean isUpdated = false;
                if ((value instanceof String && !StringUtils.isBlank((String) value)) || (value instanceof Boolean)) {
                    if ("startAt".equals(convertedKey) || "endAt".equals(convertedKey)) {
                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        value = formatter.parse(Objects.toString(value, ""));
                    }
                    field.set(event, value);
                    isUpdated = true;
                }
                if ((value instanceof Number && 0 <= ((Number) value).longValue()) && "maxTicketAmount".equals(convertedKey)) {
                    field.set(event, ((Number) value).longValue());
                    isUpdated = true;
                }
                if ((value instanceof Double && 0 <= (Double) value) && ("unitPrice".equals(convertedKey) || "childrenDiscount".equals(convertedKey) || "studentsDiscount".equals(convertedKey))) {
                    field.set(event, BigDecimal.valueOf((Double) value));
                    isUpdated = true;
                }
                if (isUpdated) {
                    log.info("Event (id=\"{}\") field \"{}\" was updated to \"{}\"", event.getId(), convertedKey, Objects.toString(value, ""));
                }
            } catch (NullPointerException e) {
                log.error("Event (id=\"{}\") was not updated - field can not be null", event.getId());
            } catch (NoSuchFieldException e) {
                log.error("Event (id=\"{}\") was not updated - field \"{}\" does not exist", event.getId(), convertedKey);
            } catch (IllegalAccessException e) {
                log.error("Event (id=\"{}\") was not updated - illegal access to field \"{}\"", event.getId(), convertedKey);
            } catch (ParseException e) {
                log.error(e.getMessage());
            }
        });

        if (event.getEndAt().before(event.getStartAt())) {
            log.error("Event was not updated - end date can not be before start date");
            throw new EventProcessingException("Event was not updated - end date can not be before start date");
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteEventById(long id) throws EventProcessingException {
        try {
            eventRepository.deleteById(id);
            log.info("Event (id=\"{}\") was deleted", id);
        } catch (RuntimeException e) {
            log.error("Event (id=\"{}\") was not deleted", id);
            throw new EventProcessingException("Event was not deleted - invalid event id");
        }
    }

    private String toCamelCase(String value) {
        String[] parts = value.split("_");
        if (parts.length < 1) {
            return "";
        }

        StringBuilder camelCaseString = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            camelCaseString.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1).toLowerCase());
        }

        return camelCaseString.toString();
    }
}
