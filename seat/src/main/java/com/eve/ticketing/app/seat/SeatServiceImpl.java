package com.eve.ticketing.app.seat;

import com.eve.ticketing.app.seat.dto.SeatFilterDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;

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
            log.error("Seat (id=\"{}\") was not found", id);
            return new SeatProcessingException("Seat was not found - invalid seat id");
        });
    }

    @Override
    @Transactional
    public void createSeat(Seat seat) throws SeatProcessingException {
        try {
            seatRepository.save(seat);
            log.info("Seat (seatId=\"{}\", eventId=\"{}\") was created/updated", seat.getId(), seat.getEventId());
        } catch (RuntimeException e) {
            log.error("Seat (seatId=\"{}\", eventId=\"{}\") was not created/updated", seat.getId(), seat.getEventId());
            throw new SeatProcessingException("Seat was not created/updated - " + e.getMessage());
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Seat updateSeat(HashMap<String, Object> values) throws SeatProcessingException {
        if (values == null) {
            log.error("Seat was not updated - empty values");
            throw new SeatProcessingException("Seat was not updated - empty values");
        }

        if (values.get("reserve") instanceof Boolean && Boolean.TRUE.equals(values.get("reserve"))
                && values.get("event_id") instanceof Number && values.get("event_id") != null
                && values.get("max_ticket_amount") instanceof Number && values.get("max_ticket_amount") != null) {
            long eventId = ((Number) values.get("event_id")).longValue();
            long maxTicketAmount = ((Number) values.get("max_ticket_amount")).longValue();
            long currentTicketAmount = seatRepository.countByEventIdAndOccupiedTrue(eventId);
            Seat seat = seatRepository.findFirstByEventIdAndOccupiedIsFalse(eventId).orElseThrow(() -> {
                log.error("Available Seat for Event (id=\"{}\") was not found", eventId);
                return new SeatProcessingException("Seat was not found - invalid event id");
            });
            if (maxTicketAmount == (currentTicketAmount + 1L)) {
                updateEvent(eventId, true);
            }
            return seat;
        }

        if (values.get("id") == null || !(values.get("id") instanceof Number)) {
            log.error("Seat id should not be null and should be a number");
            throw new SeatProcessingException("Seat id should not be null");
        }
        Seat seat = getSeatById(((Number) values.remove("id")).longValue());

        values.remove("event_id");
        values.forEach((key, value) -> {
            String convertedKey = toCamelCase(key);
            try {
                Field field = seat.getClass().getDeclaredField(convertedKey);
                field.setAccessible(true);
                boolean isUpdated = ((value instanceof Integer && 0 <= ((Integer) value)));
                if ((value instanceof Boolean)) {
                    if (Boolean.TRUE.equals(value) && Boolean.FALSE.equals(seat.getOccupied())
                            && (values.get("max_ticket_amount") instanceof Number) && values.get("max_ticket_amount") != null) {
                        long maxTicketAmount = ((Number) values.get("max_ticket_amount")).longValue();
                        long currentTicketAmount = seatRepository.countByEventIdAndOccupiedTrue(seat.getEventId());
                        if (maxTicketAmount == (currentTicketAmount + 1L)) {
                            updateEvent(seat.getEventId(), true);
                        }
                        isUpdated = true;
                    }
                    if (Boolean.FALSE.equals(value) && Boolean.TRUE.equals(seat.getOccupied())
                            && values.get("is_sold_out") instanceof Boolean && values.get("is_sold_out") != null) {
                        if (Boolean.TRUE.equals(values.get("is_sold_out"))) {
                            updateEvent(seat.getEventId(), false);
                        }
                        isUpdated = true;
                    }
                }
                if (isUpdated) {
                    field.set(seat, value);
                    log.info("Seat (id=\"{}\") field \"{}\" was updated to \"{}\"", seat.getId(), convertedKey, value);
                }
            } catch (NullPointerException e) {
                log.error("Seat (id=\"{}\") was not updated - field can not be null", seat.getId());
            } catch (NoSuchFieldException e) {
                log.error("Seat (id=\"{}\") was not updated - field \"{}\" does not exist", seat.getId(), convertedKey);
            } catch (IllegalAccessException e) {
                log.error("Seat (id=\"{}\") was not updated - illegal access to field \"{}\"", seat.getId(), convertedKey);
            }
        });

        return seat;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteSeatById(long id) throws SeatProcessingException {
        try {
            seatRepository.deleteById(id);
            log.info("Seat (id=\"{}\") was deleted", id);
        } catch (RuntimeException e) {
            log.error("Seat (id=\"{}\") was not deleted", id);
            throw new SeatProcessingException("Seat was not deleted - invalid seat id");
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
            log.error("Unable to communicate with event server - {}", e.getMessage());
            throw new SeatProcessingException("Unable to communicate with event server");
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
