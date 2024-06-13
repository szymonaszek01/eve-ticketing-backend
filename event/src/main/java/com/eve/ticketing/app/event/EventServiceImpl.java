package com.eve.ticketing.app.event;

import com.eve.ticketing.app.event.dto.AdminDto;
import com.eve.ticketing.app.event.dto.EventFilterDto;
import com.eve.ticketing.app.event.exception.Error;
import com.eve.ticketing.app.event.exception.EventProcessingException;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;

import static com.eve.ticketing.app.event.EventSpecification.*;

@Slf4j
@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final RestTemplate restTemplate;

    @Override
    public Page<Event> getEventList(int page, int size, EventFilterDto eventFilterDto) {
        Date minDate = EventUtil.getDateFromString(eventFilterDto.getMinDate());
        Date maxDate = EventUtil.getDateFromString(eventFilterDto.getMaxDate());
        Specification<Event> eventSpecification = Specification.where(eventNameEqual(eventFilterDto.getName()))
                .and(eventUnitPriceBetween(eventFilterDto.getMinUnitPrice(), eventFilterDto.getMaxUnitPrice()))
                .and(eventStartAtBetween(minDate, maxDate))
                .and(eventEndAtBetween(minDate, maxDate))
                .and(eventCountryEqual(eventFilterDto.getCountry())).and(eventAddressEqual(eventFilterDto.getAddress()));
        Pageable pageable = PageRequest.of(page, size);

        return eventRepository.findAll(eventSpecification, pageable);
    }

    @Override
    public Event getEventById(long id) throws EventProcessingException {
        return eventRepository.findById(id).orElseThrow(() -> {
            Error error = Error.builder().method("GET").field("id").value(id).description("id not found").build();
            log.error(error.toString());
            return new EventProcessingException(HttpStatus.NOT_FOUND, error);
        });
    }

    @Override
    @Transactional
    public void createEvent(Event event, String token) throws EventProcessingException, ConstraintViolationException {
        try {
            if (event.getEndAt().before(event.getStartAt())) {
                Error error = Error.builder().method("POST").field("start_at").value(event.getStartAt()).description("end date can not be before start date").build();
                log.error(error.toString());
                throw new EventProcessingException(HttpStatus.BAD_REQUEST, error);
            }

            AdminDto adminDto = getAdmin(event.getAdminId(), token);
            if (!"ADMIN".equals(adminDto.getRole())) {
                Error error = Error.builder().method("POST").field("user_id").value(event.getStartAt()).description("invalid user role").build();
                log.error(error.toString());
                throw new EventProcessingException(HttpStatus.BAD_REQUEST, error);
            }

            eventRepository.save(event);
            log.info("Event (id=\"{}\") was created", event.getId());
        } catch (RuntimeException e) {
            Error error = Error.builder().method("POST").field("").value(event).description("invalid parameters").build();
            log.error(error.toString());
            throw new EventProcessingException(HttpStatus.BAD_REQUEST, error);
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Event updateEvent(HashMap<String, Object> values) throws EventProcessingException, ConstraintViolationException {
        Error error = Error.builder().method("PUT").build();
        if (values == null) {
            error.setField("");
            error.setValue("");
            error.setDescription("empty values");
            log.error(error.toString());
            throw new EventProcessingException(HttpStatus.BAD_REQUEST, error);
        }
        if (values.get("id") == null || !(values.get("id") instanceof Number)) {
            error.setField("id");
            error.setValue(Objects.toString(values.get("id"), ""));
            error.setDescription("can not be null or has different type than Number");
            log.error(error.toString());
            throw new EventProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        Event event = getEventById(((Number) values.remove("id")).longValue());
        Stream.of("admin_id").forEach(values::remove);
        values.forEach((key, value) -> {
            String convertedKey = toCamelCase(key);
            try {
                Field field = event.getClass().getDeclaredField(convertedKey);
                field.setAccessible(true);
                error.setField(key);
                error.setValue(value);
                if (value instanceof String || value instanceof Boolean) {
                    if ("startAt".equals(convertedKey) || "endAt".equals(convertedKey)) {
                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        value = formatter.parse(Objects.toString(value, ""));
                    }
                    field.set(event, value);
                }
                if (value instanceof Number && "maxTicketAmount".equals(convertedKey)) {
                    field.set(event, ((Number) value).longValue());
                }
                if (value instanceof Double && ("unitPrice".equals(convertedKey) || "childrenDiscount".equals(convertedKey) || "studentsDiscount".equals(convertedKey))) {
                    field.set(event, BigDecimal.valueOf((Double) value));
                }
            } catch (NullPointerException e) {
                error.setDescription("field can not be null");
                log.error(error.toString());
            } catch (NoSuchFieldException e) {
                error.setDescription("field does not exists");
                log.error(error.toString());
            } catch (IllegalAccessException e) {
                error.setDescription("illegal access to field");
                log.error(error.toString());
            } catch (ParseException e) {
                error.setDescription(e.getMessage());
                log.error(error.toString());
            }
        });

        if (event.getEndAt().before(event.getStartAt())) {
            error.setField("start_at");
            error.setValue(event.getStartAt());
            error.setDescription("end date can not be before start date");
            log.error(error.toString());
            throw new EventProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        eventRepository.flush();

        return event;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteEventById(long id) throws EventProcessingException {
        try {
            eventRepository.deleteById(id);
            log.info("Event (id=\"{}\") was deleted", id);
        } catch (RuntimeException e) {
            Error error = Error.builder().method("DELETE").field("id").value(id).description("id not found").build();
            log.error(error.toString());
            throw new EventProcessingException(HttpStatus.NOT_FOUND, error);
        }
    }

    private AdminDto getAdmin(long adminId, String token) throws EventProcessingException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            return restTemplate.exchange(
                    "http://AUTH-USER/api/v1/auth-user/id/{id}",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    AdminDto.class,
                    adminId
            ).getBody();
        } catch (RestClientException e) {
            Error error = Error.builder().method("").field("user_id").value(adminId).description("unable to communicate with auth user server").build();
            log.error(error.toString());
            throw new EventProcessingException(HttpStatus.BAD_REQUEST, error);
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
