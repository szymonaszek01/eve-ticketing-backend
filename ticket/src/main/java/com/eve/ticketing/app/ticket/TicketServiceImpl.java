package com.eve.ticketing.app.ticket;

import com.eve.ticketing.app.ticket.dto.EventDto;
import com.eve.ticketing.app.ticket.dto.NotificationDto;
import com.eve.ticketing.app.ticket.dto.SeatDto;
import com.eve.ticketing.app.ticket.dto.TicketFilterDto;
import com.eve.ticketing.app.ticket.exception.Error;
import com.eve.ticketing.app.ticket.exception.TicketProcessingException;
import com.eve.ticketing.app.ticket.kafka.KafkaNotificationProducer;
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
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static com.eve.ticketing.app.ticket.TicketSpecification.*;

@Slf4j
@Service
@AllArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    private final RestTemplate restTemplate;

    private final KafkaNotificationProducer kafkaNotificationProducer;

    @Override
    public Page<Ticket> getTicketList(int page, int size, TicketFilterDto ticketFilterDto) {
        Specification<Ticket> ticketSpecification = Specification.where(ticketCodeEqual(ticketFilterDto.getCode()))
                .and(ticketFirstnameEqual(ticketFilterDto.getFirstname()))
                .and(ticketLastnameEqual(ticketFilterDto.getLastname()))
                .and(ticketPhoneNumberEqual(ticketFilterDto.getPhoneNumber()))
                .and(ticketCostBetween(ticketFilterDto.getMinCost(), ticketFilterDto.getMaxCost()));
        Pageable pageable = PageRequest.of(page, size);

        return ticketRepository.findAll(ticketSpecification, pageable);
    }

    @Override
    public Ticket getTicketById(long id) throws TicketProcessingException {
        return ticketRepository.findById(id).orElseThrow(() -> {
            Error error = Error.builder().method("GET").field("id").value(id).description("id not found").build();
            log.error(error.toString());
            return new TicketProcessingException(HttpStatus.NOT_FOUND, error);
        });
    }

    @Override
    @Transactional
    public void createTicket(Ticket ticket) throws TicketProcessingException, ConstraintViolationException {
        try {
            EventDto eventDto = getEvent(ticket.getEventId());
            ticket.setCode(UUID.randomUUID().toString());
            ticket.setCreatedAt(new Date(System.currentTimeMillis()));

            if (!Boolean.TRUE.equals(ticket.getIsAdult()) && ticket.getIsStudent()) {
                ticket.setIsStudent(false);
            }
            ticket.setCost(getTicketCost(eventDto, ticket.getIsAdult(), ticket.getIsStudent()));

            if (!eventDto.isWithoutSeats()) {
                HashMap<String, Object> seatValues = new HashMap<>(3);
                seatValues.put("event_id", ticket.getEventId());
                seatValues.put("max_ticket_amount", eventDto.getMaxTicketAmount());
                seatValues.put("reserve", true);
                SeatDto seatDto = updateSeat(seatValues);
                ticket.setSeatId(seatDto.getId());
            }

            ticketRepository.save(ticket);
            String message = "Hi " + ticket.getFirstname() + ".\nYou have reserved on the " + ticket.getCreatedAt() +
                    " a ticket with code \"" + ticket.getCode() + "\" for " + eventDto.getName() + ".\nSee yoo soon,\nEve ticketing system";
            publishNotification(ticket, message);

            log.info("Ticket (code=\"{}\", eventId={}) was created", ticket.getCode(), ticket.getEventId());
        } catch (RuntimeException e) {
            Error error = Error.builder().method("POST").field("").value(ticket).description("invalid parameters").build();
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Ticket updateTicket(HashMap<String, Object> values) throws TicketProcessingException, ConstraintViolationException {
        Error error = Error.builder().method("PUT").build();
        if (values == null) {
            error.setField("");
            error.setValue("");
            error.setDescription("empty values");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
        if (values.get("id") == null || !(values.get("id") instanceof Number)) {
            error.setField("id");
            error.setValue(Objects.toString(values.get("id"), ""));
            error.setDescription("can not be null or has different type than Number");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        Ticket ticket = getTicketById(((Number) values.remove("id")).longValue());
        Stream.of("code", "createdAt", "cost", "eventId").forEach(values::remove);
        EventDto eventDto = getEvent(ticket.getId());
        Set<String> updatedFields = new HashSet<>();
        values.forEach((key, value) -> {
            String convertedKey = toCamelCase(key);
            try {
                Field field = ticket.getClass().getDeclaredField(convertedKey);
                field.setAccessible(true);
                error.setField(key);
                error.setValue(value);
                if (value instanceof String || value instanceof Boolean) {
                    field.set(ticket, value);
                    updatedFields.add(convertedKey);
                }
                if (value instanceof Number && "seatId".equals(convertedKey) && !eventDto.isWithoutSeats()) {
                    HashMap<String, Object> seatValues = new HashMap<>(3);
                    seatValues.put("event_id", ticket.getEventId());
                    if (ticket.getSeatId() != null) {
                        seatValues.put("id", ticket.getSeatId());
                        seatValues.put("occupied", false);
                        updateSeat(seatValues);
                    }
                    seatValues.put("id", value);
                    seatValues.put("occupied", true);
                    SeatDto seatDto = updateSeat(seatValues);
                    field.set(ticket, seatDto.getId());
                    updatedFields.add(convertedKey);
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

        if (updatedFields.contains("isAdult") || updatedFields.contains("isStudent")) {
            if (!Boolean.TRUE.equals(ticket.getIsAdult()) && ticket.getIsStudent()) {
                ticket.setIsStudent(false);
            }
            ticket.setCost(getTicketCost(eventDto, ticket.getIsAdult(), ticket.getIsStudent()));
        }
        if (!updatedFields.isEmpty()) {
            String message = "Hi " + ticket.getFirstname() + ".\nYou have updated a ticket with code \"" + ticket.getCode()
                    + "\" for " + eventDto.getName() + ". Please, see the details on our page. \nSee yoo soon,\nEve ticketing system";
            publishNotification(ticket, message);
        }

        ticketRepository.flush();

        return ticket;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteTicketById(long id) throws TicketProcessingException {
        try {
            Ticket ticket = getTicketById(id);
            EventDto eventDto = getEvent(ticket.getEventId());

            if (!eventDto.isWithoutSeats()) {
                HashMap<String, Object> seatValues = new HashMap<>(4);
                seatValues.put("id", ticket.getSeatId());
                seatValues.put("event_id", ticket.getEventId());
                seatValues.put("is_sold_out", eventDto.isSoldOut());
                seatValues.put("occupied", false);
                updateSeat(seatValues);
            }

            ticketRepository.deleteById(id);
            log.info("Ticket (id=\"{}\") was deleted", id);
        } catch (RuntimeException e) {
            Error error = Error.builder().method("DELETE").field("id").value(id).description("id not found").build();
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.NOT_FOUND, error);
        }
    }

    private BigDecimal getTicketCost(EventDto eventDto, boolean isAdult, boolean isStudent) throws TicketProcessingException {
        if (!isAdult && eventDto.getChildrenDiscount() != null) {
            return eventDto.getUnitPrice().multiply(BigDecimal.ONE.subtract(eventDto.getChildrenDiscount()));
        }
        if (isAdult && isStudent && eventDto.getStudentsDiscount() != null) {
            return eventDto.getUnitPrice().multiply(BigDecimal.ONE.subtract(eventDto.getStudentsDiscount()));
        }
        if (isAdult) {
            return eventDto.getUnitPrice();
        }

        Error error = Error.builder().method("").field("cost").value("").description("unknown discounts configuration").build();
        log.error(error.toString());
        throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
    }

    private void publishNotification(Ticket ticket, String message) {
        kafkaNotificationProducer.publish(NotificationDto.builder()
                .phoneNumber(ticket.getPhoneNumber())
                .firstname(ticket.getFirstname())
                .message(message)
                .ticketId(ticket.getId())
                .build());
    }

    private EventDto getEvent(long eventId) throws TicketProcessingException {
        EventDto eventDto;
        Error error = Error.builder().method("").field("event_id").value(eventId).build();
        try {
            eventDto = restTemplate.getForObject(
                    "http://EVENT/api/v1/event/id/{id}",
                    EventDto.class,
                    eventId
            );
        } catch (RestClientException e) {
            error.setDescription("unable to communicate with event server");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        if (eventDto == null) {
            error.setDescription("id not found");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
        if (new Date(System.currentTimeMillis()).after(eventDto.getStartAt())) {
            error.setDescription("event has started");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }
        if (eventDto.isSoldOut()) {
            error.setDescription("tickets sold out");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        return eventDto;
    }

    private SeatDto updateSeat(HashMap<String, Object> values) throws TicketProcessingException {
        Long eventId = values.get("event_id") != null ? ((Number) values.get("event_id")).longValue() : null;
        Error error = Error.builder().method("").field("event_id").value(eventId).build();
        if (eventId == null) {
            error.setDescription("should not be null");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        try {
            return restTemplate.exchange(
                    "http://SEAT/api/v1/seat/update",
                    HttpMethod.PUT,
                    new HttpEntity<>(values),
                    SeatDto.class
            ).getBody();
        } catch (RestClientException e) {
            error.setDescription("unable to communicate with seat server");
            log.error(error.toString());
            throw new TicketProcessingException(HttpStatus.BAD_REQUEST, error);
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
