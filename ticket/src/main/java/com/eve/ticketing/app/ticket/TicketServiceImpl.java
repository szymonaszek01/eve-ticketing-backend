package com.eve.ticketing.app.ticket;

import com.eve.ticketing.app.ticket.dto.EventDto;
import com.eve.ticketing.app.ticket.dto.NotificationDto;
import com.eve.ticketing.app.ticket.dto.SeatDto;
import com.eve.ticketing.app.ticket.dto.TicketFilterDto;
import com.eve.ticketing.app.ticket.kafka.KafkaNotificationProducer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
            log.error("Ticket (id=\"{}\") was not found", id);
            return new TicketProcessingException("Ticket was not found - invalid ticket id");
        });
    }

    @Override
    @Transactional
    public void createTicket(Ticket ticket) throws TicketProcessingException {
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

            log.info("Ticket (code=\"{}\", eventId={}) was created/updated", ticket.getCode(), ticket.getEventId());
        } catch (RuntimeException e) {
            throw new TicketProcessingException("Ticket was not created/updated - " + e.getMessage());
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Ticket updateTicket(HashMap<String, Object> values) throws TicketProcessingException {
        if (values == null) {
            log.error("Ticket was not updated - empty values");
            throw new TicketProcessingException("Ticket was not updated - empty values");
        }

        if (values.get("id") == null || !(values.get("id") instanceof Number)) {
            log.error("Ticket id should not be null and should be a number");
            throw new TicketProcessingException("Ticket id should not be null");
        }
        Ticket ticket = getTicketById(((Number) values.remove("id")).longValue());

        Stream.of("code", "createdAt", "cost", "eventId").forEach(values::remove);
        Set<String> updatedFields = new HashSet<>();
        EventDto eventDto = getEvent(ticket.getId());
        values.forEach((key, value) -> {
            String convertedKey = toCamelCase(key);
            try {
                Field field = ticket.getClass().getDeclaredField(convertedKey);
                field.setAccessible(true);
                boolean isUpdated = false;
                if ((value instanceof String && !StringUtils.isBlank((String) value)) || (value instanceof Boolean)) {
                    field.set(ticket, value);
                    isUpdated = true;
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
                    isUpdated = true;
                }
                if (isUpdated) {
                    updatedFields.add(convertedKey);
                    log.info("Ticket (id=\"{}\") field \"{}\" was updated to \"{}\"", ticket.getId(), convertedKey, Objects.toString(value, ""));
                }
            } catch (NullPointerException e) {
                log.error("Ticket (id=\"{}\") was not updated - field can not be null", ticket.getId());
            } catch (NoSuchFieldException e) {
                log.error("Ticket (id=\"{}\") was not updated - field \"{}\" does not exist", ticket.getId(), convertedKey);
            } catch (IllegalAccessException e) {
                log.error("Ticket (id=\"{}\") was not updated - illegal access to field \"{}\"", ticket.getId(), convertedKey);
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

        return ticket;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
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
            log.error("Ticket (id=\"{}\") was not deleted", id);
            throw new TicketProcessingException("Ticket was not deleted - " + e.getMessage());
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

        log.error("Unable to calculate Ticket cost for Event (id=\"{}\")", eventDto.getId());
        throw new TicketProcessingException("Unknown discounts configuration");
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
        try {
            eventDto = restTemplate.getForObject(
                    "http://EVENT/api/v1/event/id/{id}",
                    EventDto.class,
                    eventId
            );
        } catch (RestClientException e) {
            log.error("Unable to communicate with event server - {}", e.getMessage());
            throw new TicketProcessingException("Unable to communicate with event server");
        }

        if (eventDto == null) {
            log.error("Ticket for Event (id=\"{}\") was not created - event not found", eventId);
            throw new TicketProcessingException("Event not found");
        }
        if (new Date(System.currentTimeMillis()).after(eventDto.getStartAt())) {
            log.error("Ticket for Event (id=\"{}\") was not created - event has started", eventId);
            throw new TicketProcessingException("Event has started");
        }
        if (eventDto.isSoldOut()) {
            log.error("Ticket for Event (id=\"{}\") was not created - tickets sold out", eventId);
            throw new TicketProcessingException("Tickets sold out");
        }

        return eventDto;
    }

    private SeatDto updateSeat(HashMap<String, Object> values) throws TicketProcessingException {
        Long eventId = values.get("event_id") != null ? ((Number) values.get("event_id")).longValue() : null;
        if (eventId == null) {
            log.error("Ticket was not created - eventId can not be null");
            throw new TicketProcessingException("Ticket was not created - eventId can not be null");
        }

        try {
            return restTemplate.exchange(
                    "http://SEAT/api/v1/seat/update",
                    HttpMethod.PUT,
                    new HttpEntity<>(values),
                    SeatDto.class
            ).getBody();
        } catch (RestClientException e) {
            log.error("Ticket (eventId={}) was not created - {}", eventId, e.getMessage());
            throw new TicketProcessingException("Unable to communicate with seat server");
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
