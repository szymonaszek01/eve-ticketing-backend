package com.eve.ticketing.app.ticket;

import com.eve.ticketing.app.ticket.dto.TicketFilterDto;
import org.springframework.data.domain.Page;

import java.util.HashMap;

public interface TicketService {

    Page<Ticket> getTicketList(int page, int size, TicketFilterDto ticketFilterDto);

    Ticket getTicketById(long id) throws TicketProcessingException;

    void createTicket(Ticket ticket) throws TicketProcessingException;

    Ticket updateTicket(HashMap<String, Object> values) throws TicketProcessingException;

    void deleteTicketById(long id) throws TicketProcessingException;
}
