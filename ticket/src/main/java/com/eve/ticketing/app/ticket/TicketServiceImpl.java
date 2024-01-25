package com.eve.ticketing.app.ticket;

import com.eve.ticketing.app.ticket.dto.EventShortDescriptionDto;
import com.eve.ticketing.app.ticket.dto.SeatCancelDto;
import com.eve.ticketing.app.ticket.dto.SeatReserveDto;
import com.eve.ticketing.app.ticket.dto.TicketFilterDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static com.eve.ticketing.app.ticket.TicketSpecification.*;

@Slf4j
@Service
@AllArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    private final RestTemplate restTemplate;

    @Override
    public void createOrUpdateTicket(Ticket ticket) throws TicketProcessingException {
        try {
            EventShortDescriptionDto eventShortDescriptionDto = getEventShortDescription(ticket.getEventId());
            BigDecimal cost = getTicketCost(eventShortDescriptionDto, ticket.isAdult(), ticket.isStudent());

            ticket.setCode(UUID.randomUUID().toString());
            ticket.setCreatedAt(new Date(System.currentTimeMillis()));
            ticket.setCost(cost);

            if (!eventShortDescriptionDto.isWithoutSeats() && ticket.getId() == null) {
                ticket.setSeatId(reserveSeat(SeatReserveDto.builder()
                        .eventId(eventShortDescriptionDto.getEventId())
                        .maxTicketAmount(eventShortDescriptionDto.getMaxTicketAmount())
                        .build()));
            }

            ticketRepository.save(ticket);
            log.info("Ticket (code=\"{}\", eventId={}) was created/updated", ticket.getCode(), ticket.getEventId());
        } catch (RuntimeException e) {
            throw new TicketProcessingException("Ticket was not created/updated - " + e.getMessage());
        }
    }

    @Override
    public Ticket getTicketById(long id) throws TicketProcessingException {
        return ticketRepository.findById(id).orElseThrow(() -> {
            log.error("Ticket (id=\"{}\") was not found", id);
            throw new TicketProcessingException("Ticket was not found - invalid ticket id");
        });
    }

    @Override
    public Page<Ticket> getTicketList(int page, int size, TicketFilterDto ticketFilterDto) {
        Specification<Ticket> ticketSpecification = Specification.where(ticketCodeEqual(ticketFilterDto.getCode()))
                .and(ticketFirstnameEqual(ticketFilterDto.getFirstname()))
                .and(ticketLastnameEqual(ticketFilterDto.getLastname()))
                .and(ticketIdentityCardNumberEqual(ticketFilterDto.getIdentityCardNumber()))
                .and(ticketCostBetween(ticketFilterDto.getMinCost(), ticketFilterDto.getMaxCost()));
        Pageable pageable = PageRequest.of(page, size);

        return ticketRepository.findAll(ticketSpecification, pageable);
    }

    @Override
    public void deleteTicketById(long id) throws TicketProcessingException {
        try {
            Ticket ticket = getTicketById(id);
            EventShortDescriptionDto eventShortDescriptionDto = getEventShortDescription(ticket.getEventId());
            cancelSeat(SeatCancelDto.builder()
                    .seatId(ticket.getSeatId())
                    .isSoldOut(eventShortDescriptionDto.isSoldOut())
                    .build());

            ticketRepository.deleteById(id);
            log.info("Ticket (id=\"{}\") was deleted", id);
        } catch (RuntimeException e) {
            log.error("Ticket (id=\"{}\") was not deleted", id);
            throw new TicketProcessingException("Ticket was not deleted - " + e.getMessage());
        }
    }

    private EventShortDescriptionDto getEventShortDescription(long eventId) throws TicketProcessingException {
        try {
            EventShortDescriptionDto eventShortDescriptionDto = restTemplate.getForObject(
                    "http://EVENT/api/v1/event/id/{id}/short-description",
                    EventShortDescriptionDto.class,
                    eventId
            );

            if (eventShortDescriptionDto == null) {
                log.error("Ticket for Event (id=\"{}\") was not created - event not found", eventId);
                throw new TicketProcessingException("Event not found");
            }
            if (new Date(System.currentTimeMillis()).after(eventShortDescriptionDto.getStartAt())) {
                log.error("Ticket for Event (id=\"{}\") was not created - event has started", eventId);
                throw new TicketProcessingException("Event has started");
            }
            if (eventShortDescriptionDto.isSoldOut()) {
                log.error("Ticket for Event (id=\"{}\") was not created - tickets sold out", eventId);
                throw new TicketProcessingException("Tickets sold out");
            }

            return eventShortDescriptionDto;
        } catch (RestClientException e) {
            log.error("Ticket (eventId={}) was not created - {}", eventId, e.getMessage());
            throw new TicketProcessingException("Unable to communicate with event server");
        }
    }

    private BigDecimal getTicketCost(EventShortDescriptionDto eventShortDescriptionDto, boolean isAdult, boolean isStudent) throws TicketProcessingException {
        if (!isAdult && eventShortDescriptionDto.getChildrenDiscount() != null) {
            return eventShortDescriptionDto.getUnitPrice().multiply(BigDecimal.ONE.subtract(eventShortDescriptionDto.getChildrenDiscount()));
        }
        if (isAdult && isStudent && eventShortDescriptionDto.getStudentsDiscount() != null) {
            return eventShortDescriptionDto.getUnitPrice().multiply(BigDecimal.ONE.subtract(eventShortDescriptionDto.getStudentsDiscount()));
        }
        if (isAdult) {
            return eventShortDescriptionDto.getUnitPrice();
        }

        log.error("Unable to calculate Ticket cost for Event (id=\"{}\")", eventShortDescriptionDto.getEventId());
        throw new TicketProcessingException("Unknown discounts configuration");
    }

    private Long reserveSeat(SeatReserveDto seatReserveDto) throws TicketProcessingException {
        try {
            return restTemplate.exchange(
                    "http://SEAT/api/v1/seat/reserve",
                    HttpMethod.PUT,
                    new HttpEntity<>(SeatReserveDto.builder()
                            .eventId(seatReserveDto.getEventId())
                            .maxTicketAmount(seatReserveDto.getMaxTicketAmount())
                            .build()),
                    Long.class
            ).getBody();
        } catch (RestClientException e) {
            log.error("Ticket (eventId={}) was not created - {}", seatReserveDto.getEventId(), e.getMessage());
            throw new TicketProcessingException("Unable to communicate with seat server");
        }
    }

    private void cancelSeat(SeatCancelDto seatCancelDto) {
        try {
            restTemplate.exchange(
                    "http://SEAT/api/v1/seat/cancel",
                    HttpMethod.PUT,
                    new HttpEntity<>(SeatCancelDto.builder()
                            .seatId(seatCancelDto.getSeatId())
                            .isSoldOut(seatCancelDto.getIsSoldOut())
                            .build()),
                    void.class
            );
        } catch (RestClientException e) {
            log.error("Ticket (seatId={}) was not canceled - {}", seatCancelDto.getSeatId(), e.getMessage());
            throw new TicketProcessingException("Unable to communicate with seat server");
        }
    }
}
