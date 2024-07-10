package com.eve.ticketing.app.ticket;

import com.eve.ticketing.app.ticket.dto.TicketDto;
import com.eve.ticketing.app.ticket.dto.TicketFilterDto;
import com.eve.ticketing.app.ticket.exception.TicketProcessingException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.domain.Page;

import java.util.HashMap;

public interface TicketService {

    Page<Ticket> getTicketList(int page, int size, TicketFilterDto ticketFilterDto, String[] sortArray, String token);

    Ticket getTicketById(long id) throws TicketProcessingException;

    Ticket createTicket(TicketDto ticketDto, String token) throws TicketProcessingException, ConstraintViolationException;

    Ticket updateTicket(HashMap<String, Object> values) throws TicketProcessingException, ConstraintViolationException;

    void deleteTicketById(long id) throws TicketProcessingException;
}
