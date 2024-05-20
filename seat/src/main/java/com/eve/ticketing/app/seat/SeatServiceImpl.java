package com.eve.ticketing.app.seat;

import com.eve.ticketing.app.seat.dto.SeatFilterDto;
import com.eve.ticketing.app.seat.exception.Error;
import com.eve.ticketing.app.seat.exception.SeatProcessingException;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;

import static com.eve.ticketing.app.seat.SeatSpecification.*;

@Slf4j
@Service
@AllArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;

    private final RestTemplate restTemplate;

    @Override
    public Page<Seat> getSeatList(int page, int size, SeatFilterDto seatFilterDto) {
        Specification<Seat> seatSpecification = Specification.where(seatSectorEqual(seatFilterDto.getSector()))
                .and(seatRowEqual(seatFilterDto.getRow()))
                .and(seatNumberEqual(seatFilterDto.getNumber()));
        Pageable pageable = PageRequest.of(page, size);

        return seatRepository.findAll(seatSpecification, pageable);
    }

    @Override
    public Seat getSeatById(long id) throws SeatProcessingException {
        return seatRepository.findById(id).orElseThrow(() -> {
            Error error = Error.builder().method("GET").field("id").value(id).description("id not found").build();
            log.error(error.toString());
            return new SeatProcessingException(HttpStatus.NOT_FOUND, error);
        });
    }

    @Override
    @Transactional
    public void createSeat(Seat seat) throws SeatProcessingException, ConstraintViolationException {
        try {
            seatRepository.save(seat);
            log.info("Seat (seatId=\"{}\", eventId=\"{}\") was created", seat.getId(), seat.getEventId());
        } catch (RuntimeException e) {
            Error error = Error.builder().method("POST").field("").value(seat).description("invalid parameters").build();
            log.error(error.toString());
            throw new SeatProcessingException(HttpStatus.BAD_REQUEST, error);
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Seat updateSeat(HashMap<String, Object> values) throws SeatProcessingException, ConstraintViolationException {
        Error error = Error.builder().method("PUT").build();
        if (values == null) {
            error.setField("");
            error.setValue("");
            error.setDescription("empty values");
            log.error(error.toString());
            throw new SeatProcessingException(HttpStatus.BAD_REQUEST, error);
        }
        if (values.get("reserve") instanceof Boolean && Boolean.TRUE.equals(values.get("reserve"))
                && values.get("event_id") instanceof Number && values.get("event_id") != null
                && values.get("max_ticket_amount") instanceof Number && values.get("max_ticket_amount") != null) {
            long eventId = ((Number) values.get("event_id")).longValue();
            long maxTicketAmount = ((Number) values.get("max_ticket_amount")).longValue();
            long currentTicketAmount = seatRepository.countByEventIdAndOccupiedTrue(eventId);
            Seat seat = seatRepository.findFirstByEventIdAndOccupiedIsFalse(eventId).orElseThrow(() -> {
                error.setField("");
                error.setValue(values);
                error.setDescription("available seat not found");
                log.error(error.toString());
                return new SeatProcessingException(HttpStatus.BAD_REQUEST, error);
            });
            if (maxTicketAmount == (currentTicketAmount + 1L)) {
                updateEvent(eventId, true);
            }
            return seat;
        }
        if (values.get("id") == null || !(values.get("id") instanceof Number)) {
            error.setField("id");
            error.setValue(Objects.toString(values.get("id"), ""));
            error.setDescription("can not be null or has different type than Number");
            log.error(error.toString());
            throw new SeatProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        Seat seat = getSeatById(((Number) values.remove("id")).longValue());
        values.remove("event_id");
        values.forEach((key, value) -> {
            String convertedKey = toCamelCase(key);
            try {
                Field field = seat.getClass().getDeclaredField(convertedKey);
                field.setAccessible(true);
                error.setField(key);
                error.setValue(value);
                if (value instanceof Integer) {
                    field.set(seat, value);
                }
                if (value instanceof Boolean) {
                    if (Boolean.TRUE.equals(value) && Boolean.FALSE.equals(seat.getOccupied())
                            && (values.get("max_ticket_amount") instanceof Number) && values.get("max_ticket_amount") != null) {
                        long maxTicketAmount = ((Number) values.get("max_ticket_amount")).longValue();
                        long currentTicketAmount = seatRepository.countByEventIdAndOccupiedTrue(seat.getEventId());
                        if (maxTicketAmount == (currentTicketAmount + 1L)) {
                            updateEvent(seat.getEventId(), true);
                        }
                    }
                    if (Boolean.FALSE.equals(value) && Boolean.TRUE.equals(seat.getOccupied())
                            && values.get("is_sold_out") instanceof Boolean && values.get("is_sold_out") != null) {
                        if (Boolean.TRUE.equals(values.get("is_sold_out"))) {
                            updateEvent(seat.getEventId(), false);
                        }
                    }
                    field.set(seat, value);
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
            }
        });

        return seat;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteSeatById(long id) throws SeatProcessingException {
        try {
            seatRepository.deleteById(id);
            log.info("Seat (id=\"{}\") was deleted", id);
        } catch (RuntimeException e) {
            Error error = Error.builder().method("DELETE").field("id").value(id).description("id not found").build();
            log.error(error.toString());
            throw new SeatProcessingException(HttpStatus.NOT_FOUND, error);
        }
    }

    private void updateEvent(Long eventId, boolean isSoldOut) {
        try {
            HashMap<String, Object> values = new HashMap<>(2);
            values.put("id", eventId);
            values.put("is_sold_out", isSoldOut);
            restTemplate.exchange(
                    "http://EVENT/api/v1/event/update",
                    HttpMethod.PUT,
                    new HttpEntity<>(values),
                    void.class
            );
        } catch (RestClientException e) {
            Error error = Error.builder().method("").field("event_id").value(eventId).description("unable to communicate with seat server").build();
            log.error(error.toString());
            throw new SeatProcessingException(HttpStatus.BAD_REQUEST, error);
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
