package com.eve.ticketing.app.ticket;

import com.eve.ticketing.app.ticket.dto.CurrentTicketAmountDto;
import com.eve.ticketing.app.ticket.dto.TicketFilterDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
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
    public void createTicket(Ticket ticket) throws TicketProcessingException {
        try {
            BigDecimal cost = restTemplate.getForObject(
                    "http://EVENT/api/v1/event/id/{eventId}/cost?isAdult={isAdult}&isStudent={isStudent}",
                    BigDecimal.class,
                    ticket.getEventId(),
                    ticket.isAdult(),
                    ticket.isStudent()
            );

            ticket.setCode(UUID.randomUUID().toString());
            ticket.setCreatedAt(new Date(System.currentTimeMillis()));
            ticket.setCost(cost);

            restTemplate.put(
                    "http://EVENT/api/v1/event/update/current-ticket-amount",
                    CurrentTicketAmountDto.builder().eventId(ticket.getEventId()).createdTickets(1).build()
            );

            ticketRepository.save(ticket);
            log.info("Ticket (code=\"{}\", eventId={}) was created", ticket.getCode(), ticket.getEventId());
        } catch (RuntimeException e) {
            log.error("Ticket (eventId={}) was not created", ticket.getEventId());
            throw new TicketProcessingException("Ticket was not created - invalid parameters");
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
            ticketRepository.deleteById(id);
            log.info("Ticket (id=\"{}\") was deleted", id);
        } catch (RuntimeException e) {
            log.error("Ticket (id=\"{}\") was not deleted", id);
            throw new TicketProcessingException("Ticket was not deleted - invalid event id");
        }
    }
}
